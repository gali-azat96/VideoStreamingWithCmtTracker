#include <jni.h>
#include <string>
#include <opencv2/core.hpp>

extern "C" {
JNIEXPORT jstring

JNICALL
Java_com_galiazat_diplomtest4opencvimplement_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring
JNICALL
Java_com_galiazat_diplomtest4opencvimplement_MainActivity_validate(
        JNIEnv *env,
        jobject /* this */,
        jlong addrGray,
        jlong addrRgba) {
    cv::Rect();
    cv::Mat();
    std::string hello = "Hello from validate";
    return env->NewStringUTF(hello.c_str());
}
}
