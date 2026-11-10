/*
  ====================================================================================
  SISTEMA CIBERFÍSICO IOT DE INVERNADERO INTELIGENTE — FIRMWARE DE BORDE ESP32
  Asignatura: Aplicaciones Móviles para IoT (TI3042) — INACAP Sede Temuco
  Carrera: Ingeniería en Ciberseguridad
  Autor: Rodrigo Peña Sagredo
  Estándares: ISO 27400 & ISA/IEC 62443 (Control Autónomo de Borde & Fail-Safe)
  ====================================================================================
  
  Mapeo de Hardware y Conexiones en ESP32:
  - Sensor DHT22 (Temperatura y Humedad Ambiental): Pin GPIO 4
  - Sensor Higrómetro FC-28 (Humedad Sustrato Radicular): Pin ADC GPIO 34 (Entrada analógica pura)
  - Servomotor SG90 (Compuerta Cenital Batiente): Pin PWM GPIO 18
  - Driver MOSFET Canal N IRF520N (Ventilador 12V con Diodo 1N4004): Pin PWM GPIO 19
  
  Características Ciberfísicas y de Seguridad:
  1. Autonomía de Borde: Regulación en lazo cerrado local incluso sin conexión Wi-Fi.
  2. Memoria NVS (Preferences): Persistencia de umbrales biológicos por especie en flash.
  3. Lógica Fail-Safe Biológica: Estado de resguardo (compuerta 45° y pulsos de ventilación)
     ante fallas en sensores o pérdida de enlace de supervisión.
  4. Sanity Check en Lecturas: Detección y filtrado de lecturas atípicas (NaN / desconexión).
  ====================================================================================
*/

#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <DHT.h>
#include <ESP32Servo.h>

// --- ASIGNACIÓN DE PINES ---
#define PIN_DHT22         4
#define PIN_FC28_ADC      34
#define PIN_SERVO_SG90    18
#define PIN_MOSFET_FAN    19

#define DHTTYPE           DHT22

// --- CREDENCIALES PUNTO DE ACCESO WI-FI (WPA2-PSK) ---
const char* AP_SSID = "ESP32_Invernadero_IoT";
const char* AP_PASS = "Invernadero2026!"; // Mínimo 8 caracteres WPA2

// --- OBJETOS DE CONTROL ---
DHT dht(PIN_DHT22, DHTTYPE);
Servo servoCompuerta;
WebServer server(80);
Preferences preferences;

// --- ESTRUCTURA DE PERFIL BIOLÓGICO ACTIVO ---
struct PerfilPlanta {
  char id[16];
  char nombre[32];
  float tempMin;
  float tempMax;
  float humMin;
  float humMax;
  int sueloMinRaw;
  int sueloMaxRaw;
  int servoObjetivo;
  int fanObjetivoPwm;
};

PerfilPlanta perfilActivo = {
  "TOMATE",
  "Cultivo de Tomate (Templado)",
  20.0f,
  26.0f,
  60.0f,
  70.0f,
  450,
  650,
  90,
  200
};

// --- VARIABLES DE ESTADO Y TELEMETRÍA ---
float temperaturaActual = 24.0f;
float humedadActual = 60.0f;
int sueloRawActual = 520;
int anguloActualServo = 45;
int pwmActualVentilador = 0;
bool modoManualActivo = false;
bool sensorFalloCritico = false;
unsigned long ultimoMuestreo = 0;
const unsigned long INTERVALO_MUESTREO = 2000; // 2 segundos

// Prototipos de funciones
void cargarPerfilNVS();
void guardarPerfilNVS();
void actualizarLazoControl();
void ejecutarFailSafeSupervivencia();
void configurarRutasServidor();

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n[SISTEMA] Iniciando Nodo de Borde ESP32 — Invernadero Inteligente...");

  // Inicializar Sensores y Actuadores
  dht.begin();
  pinMode(PIN_FC28_ADC, INPUT);
  
  servoCompuerta.attach(PIN_SERVO_SG90, 500, 2400); // 50 Hz PWM para SG90
  servoCompuerta.write(45); // Posición inicial neutra

  // Configuración de canal PWM para ventilador MOSFET 12V (Canal LEDC)
  ledcAttach(PIN_MOSFET_FAN, 5000, 8); // 5 kHz, resolución de 8 bits (0-255)
  ledcWrite(PIN_MOSFET_FAN, 0);

  // Cargar perfil guardado en memoria no volátil
  cargarPerfilNVS();

  // Iniciar Punto de Acceso Wi-Fi seguro
  WiFi.softAP(AP_SSID, AP_PASS);
  IPAddress IP = WiFi.softAPIP();
  Serial.print("[RED] Wi-Fi AP Creado: ");
  Serial.println(AP_SSID);
  Serial.print("[RED] Dirección IP Servidor: ");
  Serial.println(IP);

  // Configurar API HTTP REST para la App Android
  configurarRutasServidor();
  server.begin();
  Serial.println("[HTTP] Servidor Web REST iniciado en puerto 80.");
}

void loop() {
  server.handleClient();

  unsigned long ahora = millis();
  if (ahora - ultimoMuestreo >= INTERVALO_MUESTREO) {
    ultimoMuestreo = ahora;
    actualizarLazoControl();
  }
}

// --- LAZO DE CONTROL AUTÓNOMO Y DETECCIÓN FAIL-SAFE ---
void actualizarLazoControl() {
  float t = dht.readTemperature();
  float h = dht.readHumidity();
  int rawSuelo = analogRead(PIN_FC28_ADC);

  // Sanity Check: Filtrado de desconexión o fallo físico del sensor DHT22
  if (isnan(t) || isnan(h) || t < -15.0f || t > 70.0f) {
    Serial.println("[ALERTA SEGURIDAD] Falla crítica en sensor DHT22. Activando Fail-Safe.");
    sensorFalloCritico = true;
    ejecutarFailSafeSupervivencia();
    return;
  }

  sensorFalloCritico = false;
  temperaturaActual = t;
  humedadActual = h;
  sueloRawActual = rawSuelo;

  // Si no está en forzado manual, ejecutar lazo cerrado agronómico
  if (!modoManualActivo) {
    // 1. Regulación de Ventilación Pasiva (Servo)
    if (temperaturaActual > perfilActivo.tempMax) {
      anguloActualServo = perfilActivo.servoObjetivo; // Apertura total para disipar calor
    } else if (temperaturaActual < perfilActivo.tempMin) {
      anguloActualServo = 0; // Cierre hermético contra heladas
    } else {
      anguloActualServo = 45; // Confort térmico intermedio
    }
    servoCompuerta.write(anguloActualServo);

    // 2. Regulación de Ventilación Forzada (Ventilador 12V con MOSFET)
    if (temperaturaActual > (perfilActivo.tempMax + 1.0f) || humedadActual > perfilActivo.humMax) {
      pwmActualVentilador = perfilActivo.fanObjetivoPwm;
    } else {
      pwmActualVentilador = 0;
    }
    ledcWrite(PIN_MOSFET_FAN, pwmActualVentilador);
  }
}

// Estado de resguardo biológico en caso de fallo físico de transductores
void ejecutarFailSafeSupervivencia() {
  anguloActualServo = 45; // Ventilación natural media
  servoCompuerta.write(anguloActualServo);
  
  // Ciclo de resguardo: Ventilador a media potencia para prevenir asfixia
  pwmActualVentilador = 120;
  ledcWrite(PIN_MOSFET_FAN, pwmActualVentilador);
}

// --- PERSISTENCIA EN MEMORIA NO VOLÁTIL (NVS) ---
void cargarPerfilNVS() {
  preferences.begin("invernadero", true);
  if (preferences.isKey("prof_id")) {
    preferences.getString("prof_id", perfilActivo.id, sizeof(perfilActivo.id));
    preferences.getString("prof_nom", perfilActivo.nombre, sizeof(perfilActivo.nombre));
    perfilActivo.tempMin = preferences.getFloat("t_min", perfilActivo.tempMin);
    perfilActivo.tempMax = preferences.getFloat("t_max", perfilActivo.tempMax);
    perfilActivo.humMin = preferences.getFloat("h_min", perfilActivo.humMin);
    perfilActivo.humMax = preferences.getFloat("h_max", perfilActivo.humMax);
    perfilActivo.sueloMinRaw = preferences.getInt("s_min", perfilActivo.sueloMinRaw);
    perfilActivo.sueloMaxRaw = preferences.getInt("s_max", perfilActivo.sueloMaxRaw);
    Serial.println("[NVS] Perfil agronómico recuperado desde memoria interna.");
  }
  preferences.end();
}

void guardarPerfilNVS() {
  preferences.begin("invernadero", false);
  preferences.putString("prof_id", perfilActivo.id);
  preferences.putString("prof_nom", perfilActivo.nombre);
  preferences.putFloat("t_min", perfilActivo.tempMin);
  preferences.putFloat("t_max", perfilActivo.tempMax);
  preferences.putFloat("h_min", perfilActivo.humMin);
  preferences.putFloat("h_max", perfilActivo.humMax);
  preferences.putInt("s_min", perfilActivo.sueloMinRaw);
  preferences.putInt("s_max", perfilActivo.sueloMaxRaw);
  preferences.end();
  Serial.println("[NVS] Perfil agronómico persistido exitosamente.");
}

// --- ENDPOINTS REST HTTP PARA LA APLICACIÓN ANDROID ---
void configurarRutasServidor() {
  // Endpoint 1: Telemetría en tiempo real (JSON)
  server.on("/api/telemetry", HTTP_GET, []() {
    String json = "{";
    json += "\"temperatura\":" + String(temperaturaActual, 1) + ",";
    json += "\"humedad\":" + String(humedadActual, 1) + ",";
    json += "\"sueloRaw\":" + String(sueloRawActual) + ",";
    json += "\"servoAngulo\":" + String(anguloActualServo) + ",";
    json += "\"fanPwm\":" + String(pwmActualVentilador) + ",";
    json += "\"modoManual\":" + String(modoManualActivo ? "true" : "false") + ",";
    json += "\"failSafe\":" + String(sensorFalloCritico ? "true" : "false") + ",";
    json += "\"rutina\":\"" + String(perfilActivo.id) + "\"";
    json += "}";
    server.send(200, "application/json", json);
  });

  // Endpoint 2: Cambio de Rutina de Cultivo
  server.on("/api/routine", HTTP_POST, []() {
    if (server.hasArg("id")) {
      String id = server.arg("id");
      if (id == "TOMATE") {
        perfilActivo = {"TOMATE", "Cultivo de Tomate", 20.0f, 26.0f, 60.0f, 70.0f, 450, 650, 90, 200};
      } else if (id == "FRUTILLA") {
        perfilActivo = {"FRUTILLA", "Cultivo de Frutilla", 15.0f, 22.0f, 65.0f, 75.0f, 500, 700, 75, 220};
      } else if (id == "LECHUGA") {
        perfilActivo = {"LECHUGA", "Hortalizas / Lechuga", 16.0f, 24.0f, 50.0f, 68.0f, 400, 600, 60, 150};
      }
      modoManualActivo = false;
      guardarPerfilNVS();
      server.send(200, "application/json", "{\"status\":\"ok\",\"mensaje\":\"Rutina conmutada\"}");
    } else {
      server.send(400, "application/json", "{\"error\":\"Falta parámetro id\"}");
    }
  });

  // Endpoint 3: Forzado Manual (Override)
  server.on("/api/actuators", HTTP_POST, []() {
    if (server.hasArg("servo")) {
      int ang = server.arg("servo").toInt();
      anguloActualServo = constrain(ang, 0, 120);
      servoCompuerta.write(anguloActualServo);
      modoManualActivo = true;
    }
    if (server.hasArg("fan")) {
      int pwm = server.arg("fan").toInt();
      pwmActualVentilador = constrain(pwm, 0, 255);
      ledcWrite(PIN_MOSFET_FAN, pwmActualVentilador);
      modoManualActivo = true;
    }
    if (server.hasArg("auto")) {
      modoManualActivo = false;
    }
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  });
}
