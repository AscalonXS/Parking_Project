// #include <Servo.h>
#include <LiquidCrystal_I2C.h>

// ==========================================
// 1. COMPONENTS
// ==========================================
// Servo gate;
LiquidCrystal_I2C lcd(0x27, 16, 2);

// ==========================================
// 2. PIN CONFIGURATION
// ==========================================
const int trigEntrance = 8;
const int echoEntrance = 9;

const int trigExit = 6;
const int echoExit = 7;

// const int servoPin = 3; // Unplugged while servo is drying/disabled

// ==========================================
// 3. PARKING SETTINGS
// ==========================================
const int TOTAL_SLOTS = 5;
int availableSlots = TOTAL_SLOTS;

// Distance in millimeters (e.g., 100mm = 10cm detect, 140mm = 14cm clear)
const float DETECT_DISTANCE = 100.0;
const float CLEAR_DISTANCE = 140.0;

// const int GATE_CLOSED = 0;
// const int GATE_OPEN = 90;
const unsigned long GATE_OPEN_TIME = 3000;
const unsigned long CROSS_SENSOR_COOLDOWN = 2000; // 2 seconds

// ==========================================
// 4. SYSTEM VARIABLES
// ==========================================
bool entranceArmed = false;
bool exitArmed = false;
bool gateIsOpen = false;
unsigned long gateOpenedAt = 0;
unsigned long entranceBlockedUntil = 0;
unsigned long exitBlockedUntil = 0;

// ==========================================
// 5. ULTRASONIC DISTANCE FUNCTION
// ==========================================
float getDistanceMM(int trigPin, int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  long duration = pulseIn(echoPin, HIGH, 30000);
  if (duration == 0) {
    return -1;
  }
  float distance = duration * 0.343 / 2;
  return distance;
}

// ==========================================
// 6. LCD DISPLAY
// ==========================================
void showStatus() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Available: ");
  lcd.print(availableSlots);
  lcd.print("/5");

  lcd.setCursor(0, 1);
  if (availableSlots == 0) {
    lcd.print("Parking FULL!");
  } else {
    lcd.print("Welcome,Drive in");
  }
}

// ==========================================
// 7. OPEN GATE (SIMULATED WITHOUT SERVO)
// ==========================================
void openGate() {
  // gate.write(GATE_OPEN);
  gateIsOpen = true;
  gateOpenedAt = millis();
  Serial.println("GATE: OPEN (simulated)");
}

// ==========================================
// 8. CLOSE GATE (SIMULATED WITHOUT SERVO)
// ==========================================
void closeGate() {
  // gate.write(GATE_CLOSED);
  gateIsOpen = false;
  Serial.println("GATE: CLOSED (simulated)");
  showStatus();
}

// ==========================================
// 9. SETUP
// ==========================================
void setup() {
  Serial.begin(9600);

  pinMode(trigEntrance, OUTPUT);
  pinMode(echoEntrance, INPUT);
  pinMode(trigExit, OUTPUT);
  pinMode(echoExit, INPUT);

  // gate.attach(servoPin);
  // gate.write(GATE_CLOSED);

  lcd.init();
  lcd.backlight();

  lcd.setCursor(0, 0);
  lcd.print("SMART PARKING");
  lcd.setCursor(0, 1);
  lcd.print("System Starting");
  delay(2000);

  showStatus();
}

// ==========================================
// 10. MAIN LOOP
// ==========================================
void loop() {
  float entranceDistance = getDistanceMM(trigEntrance, echoEntrance);
  delay(60);
  float exitDistance = getDistanceMM(trigExit, echoExit);
  unsigned long now = millis();

  // A. ARM SENSORS WHEN AREA IS CLEAR
  if (entranceDistance < 0 || entranceDistance > CLEAR_DISTANCE) {
    entranceArmed = true;
  }
  if (exitDistance < 0 || exitDistance > CLEAR_DISTANCE) {
    exitArmed = true;
  }

  // B. ENTRANCE DETECTION
  if (now >= entranceBlockedUntil && entranceDistance > 0 && entranceDistance <= DETECT_DISTANCE && entranceArmed) {
    entranceArmed = false;
    exitBlockedUntil = now + CROSS_SENSOR_COOLDOWN;

    Serial.print("ENTRANCE DETECTED: ");
    Serial.print("AVAILABLE: ");
    Serial.print(availableSlots);

    if (availableSlots > 0) {
      availableSlots--;
      Serial.println(" | CAR ENTERING");
      Serial.println("EVENT:IN");
      Serial.print(entranceDistance);
      Serial.println(" mm");

      openGate();

      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Welcome!");
      lcd.setCursor(0, 1);
      lcd.print("Slots left: ");
      lcd.print(availableSlots);
    } else {
      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Parking FULL!");
      lcd.setCursor(0, 1);
      lcd.print("Entry denied");
      Serial.println(" | ENTRY DENIED");
    }
  }

  // C. EXIT DETECTION
  if (now >= exitBlockedUntil && exitDistance > 0 && exitDistance <= DETECT_DISTANCE && exitArmed) {
    exitArmed = false;
    entranceBlockedUntil = now + CROSS_SENSOR_COOLDOWN;

    Serial.print("EXIT DETECTED: ");
    Serial.print(exitDistance);
    Serial.println(" mm");

    if (availableSlots < TOTAL_SLOTS) {
      availableSlots++;
      Serial.println("CAR EXITING");
      Serial.println("EVENT:OUT");
      Serial.print("AVAILABLE: ");
      Serial.println(availableSlots);

      openGate();

      lcd.clear();
      lcd.setCursor(0, 0);
      lcd.print("Thank You!");
      lcd.setCursor(0, 1);
      lcd.print("Slots left: ");
      lcd.print(availableSlots);
    } else {
      Serial.println("PARKING ALREADY EMPTY");
    }
  }

  // D. AUTOMATIC GATE CLOSING
  bool entranceIsClear = entranceDistance < 0 || entranceDistance > CLEAR_DISTANCE;
  bool exitIsClear = exitDistance < 0 || exitDistance > CLEAR_DISTANCE;

  if (gateIsOpen && (millis() - gateOpenedAt >= GATE_OPEN_TIME) && entranceIsClear && exitIsClear) {
    closeGate();
  }

  delay(100);
}