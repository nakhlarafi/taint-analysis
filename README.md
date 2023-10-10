# ECSE 688 Programming Assignment 1
## Objective 
This assignment aims to use Soot to implement a taint analysis tool.

## Instructions to Use the Tool

What You Need to Get Started:
* Java Development Kit 8
* Maven
* Git

### How to Get the Code:
Use this command in your terminal to clone the repository:
`git clone https://github.com/nakhlarafi/taint-analysis.git`

### How to Run the Code:
* Run MainDriver.java.
* Make sure the file paths for Sink.txt and Source.txt in TaintAnalysis.java are correct.
* Ensure the `processDir` path in `MainDriver.java` is also correct.

## Assignment Description

#### Input Output Examples
Input source file: source.txt
```
<io.github.liliweise.Source: int sensitive()>()
<io.github.liliweise.Source: int source()>()
```
**Each line is the signature of one source API**

Input sink file: sink.txt
```
<io.github.liliweise.Source: int sink()>()
<io.github.liliweise.Sink: int sink()>()
```
**Each line is the signature of one sink API**

Input program:
```
 1.  package io.github.liliweise;  
 2.   
 3.  public class Test {  
 4.      public void test() {  
 5.          int a = Source.sensitive();  
 6.          int b = Source.source();  
 7.          int c = 3;  
 8.          int d = a * c;  
 9.          if (a > 1) {  
10.            c = b + 1;  
11.          }  
12.          int e = Source.benign();  
13.          int f = 0;  
14.          if (e > 1) {  
15.              f = e;  
16.          }  
17.          Source.sink(c);  
18.          Source.sink(f);  
19.          Source.sink(d);  
20.      }  
21.  }
```

Output:
```
——————————————————
Found a Leak in <io.github.liliweise.Test: void test()>
Source: line 6: b = staticinvoke <io.github.liliweise.Source: int source()>()
Leak: line 17: staticinvoke <io.github.liliweise.Source: void sink(int)>(c)
——————————————————
Found a Leak in <io.github.liliweise.Test: void test()>
Source: line 5: a = staticinvoke <io.github.liliweise.Source: int sensitive()>()
Leak: line 17: staticinvoke <io.github.liliweise.Source: void sink(int)>(c)
——————————————————
Found a Leak in <io.github.liliweise.Test: void test()>
Source: line 5: a = staticinvoke <io.github.liliweise.Source: int sensitive()>()
Leak: line 19: staticinvoke <io.github.liliweise.Source: void sink(int)>(d)
```
### Test Programs

The structure of the test program folder is as follows:
```
|TestPrograms
  +--- InputFiles // Lists of sources and sinks
  +--- src
     +--- ProgramToAnalyzeWithExpectedOutputs.java //Test programs with expected outputs
     +--- OpenOutputTests.java //Test programs without expected outputs
     ...
  +--- bytecode //The folder containing the compiled ProgramToAnalyzeWithExpectedOutputs and OpenOutputTests
```
InputFiles contain two files:
source.txt - the list of source API signatures
sink.txt - the list of sink API signatures

ProgramToAnalyzeWithExpectedOutputs
This folder contains the test programs that will be used to verify the correctness of your implementation, and grade your implementation.

OpenOutputTests
This folder contains the test programs that will be used to guide you answer the critical thinking questions.

[//]: # (## Submission Requirements)

[//]: # (You are required to submit:)

[//]: # (1. Your assignment report)

[//]: # (2. The source code of the programs that perform taint analysis)

[//]: # (3. To ease grading, you should provide runnable scripts or clear descriptions of how to run your programs. If we fail to run your programs, you may not be able to get any score for this part.)

[//]: # (4. Zip all the files for this assignment and submit it to MyCourses - Programming Assignment 1.)


