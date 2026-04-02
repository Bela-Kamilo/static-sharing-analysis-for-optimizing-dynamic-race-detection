#! /bin/bash
all=false
pta=false
se=false
dr=false

if  ([ $# -eq 0 ] || [ "$1" = "all" ]) && [ "$1" != "none" ] ; then 
all=true 
fi

if [ "$1" = "pta" ]; then 
pta=true 
fi

if [ "$1" = "se" ] || [ "$1" = "sideeffects" ] || ([[ "$1" = "side" ]] && [[ "$2" = "effects" ]]); then 
se=true 
fi

if [ "$1" = "all" ] || [ "$1" = "dr" ] || [ "$1" = "datarace" ] || ([[ "$1" = "data" ]] && [[ "$2" = "race" ]]); then 
dr=true 
fi

if $all || $pta; then
	javac -d PTAnalysis/01\ New  PTAnalysis/01\ New/src/*.java	
	#^ compile all .java files in "1 New/src" and place them to "1 New"
	javac -d PTAnalysis/02\ Copy  PTAnalysis/02\ Copy/src/*.java
	javac -d PTAnalysis/03\ Arrays  PTAnalysis/03\ Arrays/src/*.java
	javac -d PTAnalysis/04\ MethodInvocation  PTAnalysis/04\ MethodInvocation/src/*.java
	javac -d PTAnalysis/05\ Return  PTAnalysis/05\ Return/src/*.java
	javac -d PTAnalysis/06\ MethodAssignment  PTAnalysis/06\ MethodAssignment/src/*.java
	javac -d PTAnalysis/07\ FieldAssign  PTAnalysis/07\ FieldAssign/src/*.java
	javac -d PTAnalysis/08\ FieldRead  PTAnalysis/08\ FieldRead/src/*.java
	javac -d PTAnalysis/09\ AbstractMethods  PTAnalysis/09\ AbstractMethods/src/*.java
	javac -d PTAnalysis/10\ Other  PTAnalysis/10\ Other/src/*.java
fi

if $all || $se; then
	javac -d SideEffects/1\ Write  SideEffects/1\ Write/src/*.java
	javac -d SideEffects/2\ Read  SideEffects/2\ Read/src/*.java
	javac -d SideEffects/3\ MethodInvocation  SideEffects/3\ MethodInvocation/src/*.java
fi

if $all || $dr ; then
	javac -d DataRace/1  DataRace/1/src/*.java
fi

javac -d PTAnalysis/tmp  PTAnalysis/tmp/src/*.java
