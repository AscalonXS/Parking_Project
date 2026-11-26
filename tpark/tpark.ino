#include <LiquidCrystal_I2C.h>

// ==========================================
// 1. COMPONENTS
// ==========================================
// Servo gate;  // Disabled for now
LiquidCrystal_I2C lcd(0x27, 16, 2);

// ==========================================
// 2. PIN CONFIGURATION
// ==========================================
const int trigEntrance = 8;
const int echoEntrance = 9;

const int trigExit = 6;
const int echoExit = 7;

// const int servoPin = 3;

// ==========================================
// 3. PARKING SETTINGS
// ==========================================
const int TOTAL_SLOTS = 5;
int availableSlots = TOTAL_SLOTS;

// Distance in millimeters
const float DETECT_DISTANCE = 100.0;
const float CLEAR_DISTANCE = 140.0;

const unsigned long GATE_OPEN_TIME = 3000;
const unsigned long CROSS_SENSOR_COOLDOWN = 2000;

// How long "HELLO TURTLE" stays on LCD
const unsigned long HELLO_TURTLE_TIME = 2000;

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
// 6. NORMAL STATUS DISPLAY
// ==========================================
void showStatus() {

  lcd.clear();

  lcd.setCursor(0, 0);
  lcd.print("Available: ");
  lcd.print(availableSlots);
  lcd.print("/");
  lcd.print(TOTAL_SLOTS);

  lcd.setCursor(0, 1);

  if (availableSlots == 0) {

    lcd.print("Parking FULL!");

  } else {

    lcd.print("Welcome,Drive in");
  }
}

// ==========================================
// 7. HELLO TURTLE DISPLAY
// ==========================================
void sayHelloTurtle() {

  // Clear LCD
  lcd.clear();

  // First line
  lcd.setCursor(0, 0);
  lcd.print("HELLO TURTLE");

  // Second line
  lcd.setCursor(0, 1);
  lcd.print("Car Detected");

  // Also send to Serial Monitor
  Serial.println("HELLO TURTLE");
}

// ==========================================
// 8. OPEN GATE
// ==========================================
void openGate() {

  // gate.write(GATE_OPEN);

  gateIsOpen = true;
  gateOpenedAt = millis();

  Serial.println("GATE: OPEN (simulated)");
}

// ==========================================
// 9. CLOSE GATE
// ==========================================
void closeGate() {

  // gate.write(GATE_CLOSED);

  gateIsOpen = false;

  Serial.println("GATE: CLOSED (simulated)");

  showStatus();
}

// ==========================================
// 10. SETUP
// ==========================================
void setup() {

  Serial.begin(9600);

  // ----------------------------------------
  // Ultrasonic sensors
  // ----------------------------------------
  pinMode(trigEntrance, OUTPUT);
  pinMode(echoEntrance, INPUT);

  pinMode(trigExit, OUTPUT);
  pinMode(echoExit, INPUT);

  // ----------------------------------------
  // LCD
  // ----------------------------------------
  lcd.init();
  lcd.backlight();

  // ----------------------------------------
  // Startup message
  // ----------------------------------------
  lcd.clear();

  lcd.setCursor(0, 0);
  lcd.print("SMART PARKING");

  lcd.setCursor(0, 1);
  lcd.print("System Starting");

  delay(2000);

  // Show initial parking status
  showStatus();
}

// ==========================================
// 11. MAIN LOOP
// ==========================================
void loop() {

  // ========================================
  // READ ENTRANCE SENSOR
  // ========================================
  float entranceDistance =
      getDistanceMM(trigEntrance, echoEntrance);

  delay(60);

  // ========================================
  // READ EXIT SENSOR
  // ========================================
  float exitDistance =
      getDistanceMM(trigExit, echoExit);

  unsigned long now = millis();

  // ========================================
  // A. ARM ENTRANCE SENSOR
  // ========================================
  if (
    entranceDistance < 0 ||
    entranceDistance > CLEAR_DISTANCE
  ) {

    entranceArmed = true;
  }

  // ========================================
  // B. ARM EXIT SENSOR
  // ========================================
  if (
    exitDistance < 0 ||
    exitDistance > CLEAR_DISTANCE
  ) {

    exitArmed = true;
  }

  // ========================================
  // C. ENTRANCE DETECTION
  // ========================================
  if (
    now >= entranceBlockedUntil &&
    entranceDistance > 0 &&
    entranceDistance <= DETECT_DISTANCE &&
    entranceArmed
  ) {

    // Prevent repeated detection
    entranceArmed = false;

    // Prevent exit sensor from triggering
    exitBlockedUntil =
        now + CROSS_SENSOR_COOLDOWN;

    // --------------------------------------
    // HELLO TURTLE
    // --------------------------------------
    sayHelloTurtle();

    // Keep HELLO TURTLE on LCD
    delay(HELLO_TURTLE_TIME);

    // --------------------------------------
    // Serial information
    // --------------------------------------
    Serial.print("ENTRANCE DETECTED: ");

    Serial.print("AVAILABLE: ");
    Serial.print(availableSlots);

    // ======================================
    // CAR ENTERING
    // ======================================
    if (availableSlots > 0) {

      availableSlots--;

      Serial.println(" | CAR ENTERING");

      Serial.println("EVENT:IN");

      Serial.print("DISTANCE: ");
      Serial.print(entranceDistance);
      Serial.println(" mm");

      // Open gate
      openGate();

      // ------------------------------------
      // LCD
      // ------------------------------------
      lcd.clear();

      lcd.setCursor(0, 0);
      lcd.print("Welcome!");

      lcd.setCursor(0, 1);
      lcd.print("Slots left: ");
      lcd.print(availableSlots);

    }

    // ======================================
    // PARKING FULL
    // ======================================
    else {

      Serial.println(" | ENTRY DENIED");

      // ------------------------------------
      // LCD
      // ------------------------------------
      lcd.clear();

      lcd.setCursor(0, 0);
      lcd.print("Parking FULL!");

      lcd.setCursor(0, 1);
      lcd.print("Entry denied");
    }
  }

  // ========================================
  // D. EXIT DETECTION
  // ========================================
  if (
    now >= exitBlockedUntil &&
    exitDistance > 0 &&
    exitDistance <= DETECT_DISTANCE &&
    exitArmed
  ) {

    // Prevent repeated detection
    exitArmed = false;

    // Prevent entrance sensor from triggering
    entranceBlockedUntil =
        now + CROSS_SENSOR_COOLDOWN;

    // --------------------------------------
    // HELLO TURTLE
    // --------------------------------------
    sayHelloTurtle();

    // Keep HELLO TURTLE on LCD
    delay(HELLO_TURTLE_TIME);

    // --------------------------------------
    // Serial information
    // --------------------------------------
    Serial.print("EXIT DETECTED: ");

    Serial.print(exitDistance);
    Serial.println(" mm");

    // ======================================
    // CAR EXITING
    // ======================================
    if (availableSlots < TOTAL_SLOTS) {

      availableSlots++;

      Serial.println("CAR EXITING");

      Serial.println("EVENT:OUT");

      Serial.print("AVAILABLE: ");
      Serial.println(availableSlots);

      // Open gate
      openGate();

      // ------------------------------------
      // LCD
      // ------------------------------------
      lcd.clear();

      lcd.setCursor(0, 0);
      lcd.print("Thank You!");

      lcd.setCursor(0, 1);
      lcd.print("Slots left: ");
      lcd.print(availableSlots);
    }

    // ======================================
    // PARKING ALREADY EMPTY
    // ======================================
    else {

      Serial.println("PARKING ALREADY EMPTY");

      lcd.clear();

      lcd.setCursor(0, 0);
      lcd.print("Parking Empty");

      lcd.setCursor(0, 1);
      lcd.print("No car inside");
    }
  }

  // ========================================
  // E. AUTOMATIC GATE CLOSING
  // ========================================
  bool entranceIsClear =
      entranceDistance < 0 ||
      entranceDistance > CLEAR_DISTANCE;

  bool exitIsClear =
      exitDistance < 0 ||
      exitDistance > CLEAR_DISTANCE;

  if (
    gateIsOpen &&
    (millis() - gateOpenedAt >= GATE_OPEN_TIME) &&
    entranceIsClear &&
    exitIsClear
  ) {

    closeGate();
  }

  // ========================================
  // LOOP DELAY
  // ========================================
  delay(100);
}