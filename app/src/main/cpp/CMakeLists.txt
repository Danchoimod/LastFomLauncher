cmake_minimum_required(VERSION 3.22)
project(levi LANGUAGES CXX)

set(CMAKE_BUILD_TYPE RelWithDebInfo CACHE STRING "Build type" FORCE)

add_subdirectory(unpaircore)
add_subdirectory("../../../../../../../Users/Phu Pham/Downloads/LeviLaunchroid-5371ad40a7ed4c531dde2d97a8016b62e0c5e856/LeviLaunchroid-5371ad40a7ed4c531dde2d97a8016b62e0c5e856/app/src/main/cpp/preloader")
add_subdirectory(levi_init)
