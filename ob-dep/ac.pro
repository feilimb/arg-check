
# The path is relative to the location of this file
# injars is the location  of the folder which contains the files to be obfuscated
-injars 'C:\Temp\ac\ac.jar'

# outjars is where the obfuscated files will be put by Proguard
-outjars 'C:\Temp\ac\ac_ob.jar'

# Include the path to JAVA (have JAVA_HOME set on machine) 
-libraryjars 'C:\Program Files (x86)\Java\jre7\lib\rt.jar'

# Include the path to the eclipse distribution including all pas plugins that may be referenced.
-libraryjars '..\lib\commons-lang-2.6.jar'
-libraryjars '..\lib\commons-logging-1.1.jar'
-libraryjars '..\lib\httpclient-4.1.3.jar'
-libraryjars '..\lib\httpcore-4.1.3.jar'
-libraryjars '..\lib\ac\lib\httpmime-4.1.3.jar'

-keep public class com.jargha.argcheck.Launcher {
    public static void main(java.lang.String[]);
}

#General flags
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontshrink
-dontoptimize
-dontusemixedcaseclassnames
-dontpreverify
-verbose
-ignorewarnings

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
    native <methods>;
}

-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}