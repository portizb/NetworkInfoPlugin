LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# compile as arm code...
LOCAL_ARM_MODE := arm
LOCAL_MODULE := os-jni
LOCAL_SRC_FILES := os-jni.c

include $(BUILD_SHARED_LIBRARY)
