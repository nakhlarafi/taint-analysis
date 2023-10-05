# ECSE 688 Programming Assignment 1
## Submission Deadline
**Oct 11 23:59** Please submit your assignment via MyCourses. Late submissions will lead to penalties. 10% penalty per 24 hours of late submission. Submissions received more than 72 hours beyond the deadline will not be considered.

## Objective and Requirement
This assignment aims to give you hands-on experience with static program analysis. You are required to use Soot to implement a taint analysis tool, and write an assignment report.

## Assignment Description

In this assignment, you need to (1) implement a static taint analyzer to report sensitive information leaks, (2) test your taint analyzer with given input-output examples, and (3) prepare an assignment report illustrating your taint analyzer design as well as answering several questions.

### Implementing Taint Analysis with Soot
You are required to implement an intra-procedural static taint analyzer based on [Soot](http://soot-oss.github.io/soot/). The analyzer should take a file of source API signature list, a file of sink API signature list, and a Java program to be analyzed as input. It should output warnings of sensitive information leaks where sources can flow to sinks. One output warning should include the location of the source and sink (See examples below). 

#### Implementation requirements
- You are required to implement an **intra-procedural** taint analysis
- Your analyzer should support both explicit flows (data flow) and implicit flows (control flows).
- For implicit flows, you are only required to handle one layer of conditional statements, i.e., there will be no nested conditional statements.
- There can be multiple locations of different sources within a method, your implementation should be able to distinguish the different sources.
- Your implementation should be able to parse the contents in source API signature list and sink API signature list. Your implementation should be able to handle different input source and sink list files.
- The output warnings should contain both the source and the sink locations (line number + line content). The line content should be in Jimple form.
- The output should also indicate the method signature containing the leakage (the sink).
- Your program should take three parameters as input:
 ```SOURCE_FILE SINK_FILE INPUT_PROGRAM```

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
### Assignment Report
In your assignment report, you should include the following information:
 - Instructions to run your code
 - What oprations did you consider? What transfer functions did you implement? Why?
 - How did you handle implicit flows?
 - Answer critical thinking questions:
	 - We provide a set of programs as open-output tests. You are not required to achieve any specific outputs on these programs. They are to guide you think about limitations and trade-offs of static taint analysis. In the report, include your answers to:
		 - What does your taint analyzer output on each open-output test?
		 - What do you think should be the correct output?
		 - In what situations, static taint analysis can generate incorrect results?
 - Whether you used generative AI models to help you with any part of this assignment. If so, please report how you used the generative AI models:
   - Include screenshots of all the prompts you sent and the results you received
   - Discuss how you would evaluate the results generated by the AI models

### Test Programs

We provide test programs for you to verify your taint analysis implementation. The test programs are available in folder TestPrograms.

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

## Submission Requirements
You are required to submit:
1. Your assignment report
2. The source code of the programs that perform taint analysis
3. To ease grading, you should provide runnable scripts or clear descriptions of how to run your programs. If we fail to run your programs, you may not be able to get any score for this part.
4. Zip all the files for this assignment and submit it to MyCourses - Programming Assignment 1.

## Marking Scheme
1. Assignment report (40%)
	a. The clarity of the report introducing your transfer functions
	b. The clarity of the report introducing your design to handle implicit flows
	c. The correctness and clarity of your answers to the critical thinking questions
2. The correctness of your intra-procedural taint analysis implementations (60%)
	a. Your implementation will be assessed with the given test programs specified with expected outputs (`ExpectedOutputTests.java`). Folder `src` contains the source code of the test programs and `bytecode` contents the .class files that are compiled from the source code. You can set the path to `bytecode` as the process dir for Soot.
	b. Your grade will be (the percentage of passed test programs) * 60%
3. You should not hardcode the output your program output (Directly print the expected output). If hardcoding is found, you will lose your score for the project implementation.

## Implementation Guideline
**Basic Taint Analysis in Class**
In class, we introduced how to implement a basic taint analysis tool with Soot. It is possible to pass some test programs with that simple implementation. You may start with the example introduced in class and then implement the missing features.

**Code Skeleton**
We also provide a code skeleton to implement intra-procedural taint analysis with Soot. The skeleton is available in folder `TaintAnalysisScratch`. You can import the project into IntelliJ.

NOTE:
1. Install Java 8 and IntelliJ to run the project
2. You may need to fix the project SDK setting to properly compile the and run the project.
3. The skeleton is implemented in Java 8. To ensure better support by Soot, we implement and compile the programs in Java 8 for this assignment.
4. You may need to properly setup JAVA_HOME to make Soot run properly

 **How to get the line number of a Jimple Statement?**
Soot provides APIs to read the line number of each Jimple code. To ask Soot keep the line number information, you should anable option `keep-line-number` before running Soot:

```
Options.v().set_keep_line_number(true);
soot.Main.v().run(sootArgs);
```

Then, when traversing the Jimple code, you can get the line number of each unit by `getTag`:
```
LineNumberTag lineNumberTag = (LineNumberTag)unit.getTag("LineNumberTag");
int lineNumber = lineNumberTag.getLineNumber();
```
 **Soot is multi-threaded**
 The analysis on each method in jtp phase of Soot will be executed in parallel. Bear this in mind during your implementation to avoid concurrency bugs.
 
 **Soot Tutorial**
 Soot wiki: https://github.com/soot-oss/soot/wiki
 Soot JavaDoc & options: https://soot-oss.github.io/soot/docs/
