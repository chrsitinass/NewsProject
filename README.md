**NewsProject_v1.0**
--------------------

A pipeline framework to implement several basic NLP tasks on online news using open source NLP tools. 

**Contents**

    ├── META-INF
    ├── NewsProject.jar: executable jar file
    ├── bin
    ├── classifiers: modals of StanfordNLP NER tools
    ├── data: no need in version1.0
    ├── lib: StanfordNLP NER tools and ANSJ word segmentation tools
    ├── library: dictionaries for ANSJ
    │   └── default.dic
    ├── out
    │   └── production
    │       └── NewsProject1.0
    ├── src: core code
    │   ├── EntityLinking.java
    │   ├── EntityToTopic.java: no ready in this version
    │   ├── Mysql.java
    │   ├── NERDemo.java
    │   ├── NERDemo_Chinese.java
    │   ├── News.java
    │   ├── ReadNews.java
    │   ├── Segmentation.java
    │   └── Topic.java
    └── xmlOut: sample input files
        └── 2015-08-11.xml: show the xml format
    

**Build**
Java 1.8

    java -jar NewsProject.jar

