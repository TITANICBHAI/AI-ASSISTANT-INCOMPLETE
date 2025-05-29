cmake_minimum_required(VERSION 3.10.2)

project("aiassistant")

# Include Android log library
find_library(log-lib log)

# Define native-lib library
add_library(native-lib SHARED native-lib.cpp)

# Link libraries
target_link_libraries(native-lib ${log-lib})
