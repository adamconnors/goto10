# goto10
Ridiculously simple javascript controller for Android Things.


<h1>Quick Start</h1>
<pre>
git clone https://github.com/adamconnors/goto10.git
cd goto10/
export ANDROID_HOME='[path-to-your-android-sdk]'
./gradlew build
adb connect Android.local
adb install app/build/outputs/apk/app-debug.apk
adb shell am start -n com.shoeboxscientist.goto10/com.shoeboxscientist.goto10.MainActivity
</pre>

(If adb connect Android.local doesn't work try adb connect [raspberry-pi-ip-address]:5555) instead.

Assuming it worked, the rainbow hat will display read: REDY

Now point your Web browser to: <pre>http://[your-raspberry-ip-address]:8080</pre> for a handy-dandy javascript interface 
for controlling your Rainbow Hat.

Very basic right now, work threads & events coming soon...(ish).
