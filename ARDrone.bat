
@rem ARDrone RTC実行用スクリプト

@set CLASSPATH=bin;jar\rtcd.jar;jar\rtcprof.jar;jar\OpenRTM-aist-1.1.0.jar;jar\commons-cli-1.1.jar;jar\ARDroneForP5.jar;jar\commons-logging-1.2.jar;jar\commons-net-1.4.1.jar;jar\slf4j-api-1.7.7.jar;jar\xuggle-xuggler-5.4.jar;jar\xuggle-xuggler-arch-i686-w64-mingw32.jar;jar\xuggle-xuggler-noarch-5.4.jar

java ARDroneComp -f rtc.conf

pause;
