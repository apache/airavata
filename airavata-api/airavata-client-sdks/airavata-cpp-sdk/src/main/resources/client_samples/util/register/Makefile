CC 	       = gcc
CXX		   = g++
THRIFT_VER = thrift-0.9.2
USR_DIR    = ${HOME}/usr
THRIFT_DIR = ${USR_DIR}/${THRIFT_VER}
INCS_DIRS  = -I../lib -I/usr/include/thrift -I/usr/local/include
LINK       = g++
LFLAGS 	   = -w -Wall -std=c++11 -Wno-write-strings -DHAVE_CONFIG_H -DHAVE_INTTYPES_H -DHAVE_NETINET_IN_H
LIBS_DIRS  = -L/usr/local/lib 
CPP_DEFS   = -D=HAVE_CONFIG_H
CPP_OPTS   = -Wall -O2
LIBS       = `pkg-config --cflags glib-2.0` -L/usr/local/lib -lthrift 

GEN_SRC    = Register.cpp

####### Output directory

OBJECTS_DIR   = ./

####### Files

SOURCES 	= Register.cpp \
	../lib/thrift/transport/TSocket.cpp 

OBJECTS 	= Register.o \
	
TARGET 		= register

$(TARGET):   
	$(LINK) ${INCS_DIRS} $(LFLAGS) $(OBJCOMP) $(GEN_SRC) -o $(OBJECTS) $(LIBS)