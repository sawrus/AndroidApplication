LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := Application
LOCAL_SRC_FILES := main.c

include $(BUILD_SHARED_LIBRARY)