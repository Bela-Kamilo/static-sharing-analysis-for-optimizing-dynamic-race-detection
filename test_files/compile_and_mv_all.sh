
all=false
pta=false
se=false
dr=false

if  [ $# -eq 0 ] || [ "$1" = "all" ] ; then 
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
	javac -d PTAnalysis/1\ New  PTAnalysis/1\ New/src/*.java	
	#^ compile all .java files in "1 New/src" and place them to "1 New"
	javac -d PTAnalysis/2\ Copy  PTAnalysis/2\ Copy/src/*.java
	javac -d PTAnalysis/3\ Arrays  PTAnalysis/3\ Arrays/src/*.java
	javac -d PTAnalysis/4\ MethodInvocation  PTAnalysis/4\ MethodInvocation/src/*.java
	javac -d PTAnalysis/5\ Return  PTAnalysis/5\ Return/src/*.java
	javac -d PTAnalysis/6\ MethodAssignment  PTAnalysis/6\ MethodAssignment/src/*.java
	javac -d PTAnalysis/7\ FieldAssign  PTAnalysis/7\ FieldAssign/src/*.java
	javac -d PTAnalysis/8\ FieldRead  PTAnalysis/8\ FieldRead/src/*.java
	javac -d PTAnalysis/9\ Other  PTAnalysis/9\ Other/src/*.java
fi

if $all || $se; then
	javac -d SideEffects/1\ Write  SideEffects/1\ Write/src/*.java
	javac -d SideEffects/2\ Read  SideEffects/2\ Read/src/*.java
	javac -d SideEffects/3\ MethodInvocation  SideEffects/3\ MethodInvocation/src/*.java
fi

if $all || $dr ; then
	javac -d DataRace/1  DataRace/1/src/*.java
fi
