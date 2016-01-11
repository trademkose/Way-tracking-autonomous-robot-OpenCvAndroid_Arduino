#include <Adafruit_CC3000.h>
#include <Adafruit_CC3000_Server.h>
#include <ccspi.h>

//with Buzzer melody
/***************************************************
  Adafruit CC3000 Breakout/Shield TCP Chat Server
    
  This is a simple chat server which allows clients to connect
  with telnet and exchange messages.  Anything sent by one
  client will be written out to all connected clients.

  See the CC3000 tutorial on Adafruit's learning system
  for more information on setting up and using the
  CC3000:
    http://learn.adafruit.com/adafruit-cc3000-wifi  
    
  Requirements:
  
  This sketch requires the Adafruit CC3000 library.  You can
  download the library from:
    https://github.com/adafruit/Adafruit_CC3000_Library
  
  For information on installing libraries in the Arduino IDE
  see this page:
    http://arduino.cc/en/Guide/Libraries
  
  Usage:
    
  Update the SSID and, if necessary, the CC3000 hardware pin 
  information below, then run the sketch and check the 
  output of the serial port.  After connecting to the 
  wireless network successfully the sketch will output 
  the IP address of the server and start listening for 
  connections.  Once listening for connections, connect
  to the server from your computer  using a telnet client
  on port 23.  
           
  For example on Linux or Mac OSX, if your CC3000 has an
  IP address 192.168.1.100 you would execute in a command
  window:
  
    telnet 192.168.1.100 23
           
  Connect multiple clients and notice that whatever one client
  sends will be echoed to all other clients.  Press ctrl-] and 
  type quit at the prompt to close the telnet session.
           
  On Windows you'll need to download a telnet client.  PuTTY 
  is a good, free GUI client: 
    http://www.chiark.greenend.org.uk/~sgtatham/putty/
  
  License:
 
  This example is copyright (c) 2013 Tony DiCola (tony@tonydicola.com)
  and is released under an open source MIT license.  See details at:
    http://opensource.org/licenses/MIT
  
  This code was adapted from Adafruit CC3000 library example 
  code which has the following license:
  
  Designed specifically to work with the Adafruit WiFi products:
  ----> https://www.adafruit.com/products/1469

  Adafruit invests time and resources providing this open source code, 
  please support Adafruit and open-source hardware by purchasing 
  products from Adafruit!

  Written by Limor Fried & Kevin Townsend for Adafruit Industries.  
  BSD license, all text above must be included in any redistribution      
 ****************************************************/
#include <Adafruit_CC3000.h>
#include <SPI.h>
#include "utility/debug.h"
#include "utility/socket.h"

// These are the interrupt and control pins
#define ADAFRUIT_CC3000_IRQ   3  // MUST be an interrupt pin!
// These can be any two pins
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10
// Use hardware SPI for the remaining pins
// On an UNO, SCK = 13, MISO = 12, and MOSI = 11
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed

//#define WLAN_SSID       "AirTies_Air5341"           // cannot be longer than 32 characters!
//#define WLAN_PASS       "VHEBIWEn1V"
#define WLAN_SSID       "mahmut"           // cannot be longer than 32 characters!
#define WLAN_PASS       "mahmut123"
// Security can be WLAN_SEC_UNSEC, WLAN_SEC_WEP, WLAN_SEC_WPA or WLAN_SEC_WPA2
#define WLAN_SECURITY   WLAN_SEC_WPA2

#define LISTEN_PORT           23    // What TCP port to listen on for connections.

Adafruit_CC3000_Server chatServer(LISTEN_PORT);
// TONES  ==========================================
// Start by defining the relationship between 
//       note, period, &  frequency. 
#define  c     3830    // 261 Hz 
#define  d     3400    // 294 Hz 
#define  e     3038    // 329 Hz 
#define  f     2864    // 349 Hz 
#define  g     2550    // 392 Hz 
#define  a     2272    // 440 Hz 
#define  b     2028    // 493 Hz 
#define  C     1912    // 523 Hz 
// Define a special note, 'R', to represent a rest
#define  R     0
int in1=2;
int in2=7;
int in3=8;
int in4=9;
int led=6;
int hiz_donme=5450;
int hiz_duz=5950;
void setup(void)
{
  
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);
  pinMode(in3, OUTPUT);
  pinMode(in4,OUTPUT);  
  pinMode(led,OUTPUT);
   pinMode(A0,OUTPUT);
  Serial.begin(115200);
  Serial.println(F("Hello, CC3000!\n")); 

  Serial.print("Free RAM: "); Serial.println(getFreeRam(), DEC);
  
  /* Initialise the module */
  Serial.println(F("\nInitializing..."));
  if (!cc3000.begin())
  {
    Serial.println(F("Couldn't begin()! Check your wiring?"));
    while(1);
  }
  
  Serial.print(F("\nAttempting to connect to ")); Serial.println(WLAN_SSID);
  if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)) {
    Serial.println(F("Failed!"));
    while(1);
  }
   
  Serial.println(F("Connected!"));
  
  Serial.println(F("Request DHCP"));
  while (!cc3000.checkDHCP())
  {
    delay(100); // ToDo: Insert a DHCP timeout!
  }  

  /* Display the IP address DNS, Gateway, etc. */  
  while (! displayConnectionDetails()) {
    delay(1000);
  }
  
  /*********************************************************/
  /* You can safely remove this to save some flash memory! */
  /*********************************************************/
  Serial.println(F("\r\nNOTE: This sketch may cause problems with other sketches"));
  Serial.println(F("since the .disconnect() function is never called, so the"));
  Serial.println(F("AP may refuse connection requests from the CC3000 until a"));
  Serial.println(F("timeout period passes.  This is normal behaviour since"));
  Serial.println(F("there isn't an obvious moment to disconnect with a server.\r\n"));

  // Start listening for connections
  chatServer.begin();
  
  Serial.println(F("Listening for connections..."));
   digitalWrite(led,HIGH);
}
int count=0;
// MELODY and TIMING  =======================================
//  melody[] is an array of notes, accompanied by beats[], 
//  which sets each note's relative length (higher #, longer note) 
int melody[] = {  C,  b,  g,  C,  b,   e,  R,  C,  c,  g, a, C };
int beats[]  = { 16, 16, 16,  8,  8,  16, 32, 16, 16, 16, 8, 8 }; 
int MAX_COUNT = sizeof(melody) / 2; // Melody length, for looping.

// Set overall tempo
long tempo = 10000;
// Set length of pause between notes
int pause = 1000;
// Loop variable to increase Rest length
int rest_count = 100; //<-BLETCHEROUS HACK; See NOTES

// Initialize core variables
int tone_ = 0;
int beat = 0;
long duration  = 0;

// PLAY TONE  ==============================================
// Pulse the speaker to play a tone for a particular duration
void playTone() {
  long elapsed_time = 0;
  if (tone_ > 0) { // if this isn't a Rest beat, while the tone has 
    //  played less long than 'duration', pulse speaker HIGH and LOW
    while (elapsed_time < duration) {

      digitalWrite(A0,HIGH);
      delayMicroseconds(tone_ / 2);

      // DOWN
      digitalWrite(A0, LOW);
      delayMicroseconds(tone_ / 2);

      // Keep track of how long we pulsed
      elapsed_time += (tone_);
    } 
  }
  else { // Rest beat; loop times delay
    for (int j = 0; j < rest_count; j++) { // See NOTE on rest_count
      delayMicroseconds(duration);  
    }                                
  }                                 
}

void loop(void)
{
  // Try to get a client which is connected.
  Adafruit_CC3000_ClientRef client = chatServer.available();
  if (client) {
     // Check if there is data available to read. 
     if (client.available() > 0) {
       // Read a byte and write it to all clients.       
        uint8_t ch = client.read();
        count++;
       chatServer.write(ch);
       Serial.println(ch);
        // Serial.println(("Count:"));
       //  Serial.println(count);
        digitalWrite(led,HIGH);
       if(ch=='w'){
         
           //ileri
           for(int i=0;i<hiz_duz;i++){           
                digitalWrite(in1,LOW);
                digitalWrite(in2,HIGH);
                digitalWrite(in3,LOW);
                digitalWrite(in4,HIGH);
           }
           
          digitalWrite(in1,LOW);
          digitalWrite(in2,LOW);
          digitalWrite(in3,LOW);
          digitalWrite(in4,LOW);
           Serial.println("ileri");
           
       }else if(ch=='s'){
         
           //geri
           for(int i=0;i<hiz_duz;i++){
                 digitalWrite(in1,HIGH);
                digitalWrite(in2,LOW);
                digitalWrite(in3,HIGH);
                digitalWrite(in4,LOW);
           }           
          digitalWrite(in1,LOW);
          digitalWrite(in2,LOW);
          digitalWrite(in3,LOW);
          digitalWrite(in4,LOW);
          Serial.println("geri");
         
       }else if(ch=='q'){
         
           //ileri sol
            for(int i=0;i<hiz_donme;i++){
                
                digitalWrite(in1,LOW);
                digitalWrite(in2,LOW);
                digitalWrite(in3,LOW);
                digitalWrite(in4,HIGH);
           }
           
          digitalWrite(in1,LOW);
          digitalWrite(in2,LOW);
          digitalWrite(in3,LOW);
          digitalWrite(in4,LOW);
           Serial.println("ileri sol");
         
       }else if(ch=='e'){
         
         //ileri sag
           for(int i=0;i<hiz_donme;i++){
                digitalWrite(in1,LOW);
                digitalWrite(in2,HIGH);
                digitalWrite(in3,LOW);
                digitalWrite(in4,LOW);
        
           }
           
          digitalWrite(in1,LOW);
          digitalWrite(in2,LOW);
          digitalWrite(in3,LOW);
          digitalWrite(in4,LOW);
         Serial.println("ileri sag");
         
       }else if(ch=='z'){
         //geri sol
          for(int i=0;i<hiz_donme;i++){
               digitalWrite(in1,HIGH);
                digitalWrite(in2,LOW);
                digitalWrite(in3,LOW);
                digitalWrite(in4,LOW);
        
           }           
          digitalWrite(in1,LOW);
          digitalWrite(in2,LOW);
          digitalWrite(in3,LOW);
          digitalWrite(in4,LOW);
         Serial.println("geri sol");
       }else if(ch=='c'){
         //geri sag
           for(int i=0;i<hiz_donme;i++){
          digitalWrite(in1,LOW);
                digitalWrite(in2,LOW);
                digitalWrite(in3,HIGH);
                digitalWrite(in4,LOW);
           }           
          digitalWrite(in1,LOW);
          digitalWrite(in2,LOW);
          digitalWrite(in3,LOW);
          digitalWrite(in4,LOW);
         Serial.println("geri sag");
       }else if(ch=='b'){
         
         for (int i=0; i<MAX_COUNT; i++) {
            tone_ = melody[i];
            beat = beats[i];        
            duration = beat * tempo; // Set up timing        
            playTone(); 
            // A pause between notes...
            delayMicroseconds(pause);           
          }         
         Serial.println("buzzer");
       }
         
       
     }
     
     digitalWrite(led,LOW);
     digitalWrite(A0,LOW);
  }
}


/**************************************************************************/
/*!
    @brief  Tries to read the IP address and other connection details
*/
/**************************************************************************/
bool displayConnectionDetails(void)
{
  uint32_t ipAddress, netmask, gateway, dhcpserv, dnsserv;
  
  if(!cc3000.getIPAddress(&ipAddress, &netmask, &gateway, &dhcpserv, &dnsserv))
  {
    Serial.println(F("Unable to retrieve the IP Address!\r\n"));
    return false;
  }
  else
  {
    Serial.print(F("\nIP Addr: ")); cc3000.printIPdotsRev(ipAddress);
    Serial.print(F("\nNetmask: ")); cc3000.printIPdotsRev(netmask);
    Serial.print(F("\nGateway: ")); cc3000.printIPdotsRev(gateway);
    Serial.print(F("\nDHCPsrv: ")); cc3000.printIPdotsRev(dhcpserv);
    Serial.print(F("\nDNSserv: ")); cc3000.printIPdotsRev(dnsserv);
    Serial.println();
    return true;
  }
}
