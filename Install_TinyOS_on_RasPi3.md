Raspberry Pi - TinyOS 설치하기
=====
##설치환경
- 하드웨어 : Raspberry Pi 3 Model B
- 운영체제 : Raspbian Jessie Lite, 2016-09-23 release
- 사용 소프트웨어 버전
	- Java SDK 8
	- Python 3.5.2
	- TinyOS Version 3

##TinyOS와 nesC 설치를 위한 Pre-requisites
- git
- Java SDK
- automake
- emacs
- bison
- flex
- gperf
- gcc-msp430

~~~~
$ sudo apt-get update
$ sudo apt-get upgrade
~~~~
####패키지 설치
~~~~
$ sudo apt-get install git openjdk-8-jdk python python-serial python-usb
$ sudo apt-get install automake emacs bison flex gperf gcc-msp430
~~~~
##Python3 설치
####1. Python 패키지 설치
~~~~
$ sudo apt-get install libbz2-dev liblzma-dev libsqlite3-dev libncurses5-dev 
$ sudo apt-get install libgdbm-dev zlib1g-dev libreadline-dev libssl-dev tk-dev
~~~~
####2. Python 다운로드
~~~~
$ mkdir ~/python3
$ cd ~/python3
$ wget https://www.python.org/ftp/python/3.5.2/Python-3.5.2.tar.xz
$ tar xvf Python-3.5.2.tar.xz
~~~~
####3. Python 빌드
~~~~
$ cd Python-3.5.2
$ ./configure
$ make
$ sudo make install
~~~~
##nesC 설치
####1. nesC GitHub 다운로드
~~~~
$ git clone git://github.com/tinyos/nesc.git
~~~~
####2. nesC 빌드
~~~~
$ cd nesc
$ ./Bootstrap
$ ./configure
$ make
$ make install
~~~~
##TinyOS 설치
####1. TinyOS GitHub 다운로드
~~~~
$ git clone git://github.com/tinyos/tinyos-main.git
~~~~
####2. TinyOS 빌드
~~~~
$ cd tinyos-main/tools
$ ./Bootstrap
$ ./configure
$ make
$ make install
~~~~
##TinyOS 환경변수 설정
####1. tinyos.sh 파일 생성 후 Shell Script 작성
~~~~
export TOSROOT="<local-tinyos-path>"
export TOSDIR="$TOSROOT/tos"
export CLASSPATH=$CLASSPATH:$TOSROOT/tools/tinyos/java/tinyos.jar:.
export MAKERULES="$TOSROOT/support/make/Makerules"
export PYTHONPATH=$PYTHONPATH:$TOSROOT/tools/tinyos/java/python
echo "setting up TinyOS on source path $TOSROOT"
~~~~
####2. TOSROOT 폴더 권한 변경
~~~~
$ sudo chmod 777 /tinyos-main
~~~~
####3. bashrc 파일에 tinyos.sh 추가
~~~~
$ sudo nano ~/.bashrc
 > source <local-tinyos-path>/tinyos.sh
 파일 맨 밑에 추가
~~~~
####4. Serial Communication을 위한 경로 설정
- 시리얼 통신을 위해서 libtoscomm.so와 libgetenv.so 파일이 필요하다.
~~~~
sudo nano flush.sh
> java net.tinyos.tools.Listen -comm serial@/dev/ttyUSB0:telosb
  내용 추가 후 저장
./flush.sh
~~~~
- 파일의 경로를 찾지 못한다면 두 파일의 경로를 찾아 복사해준다.
- "TOSROOT/tools/tinyos/jni/env/", “TOSROOT/tools/tinyos/jni/serial/” 확인
~~~~
$ find -name libtoscomm.so 
$ find -name libgetenv.so
$ cp <find-path>/libtoscomm.so /usr/lib
$ cp <find-path>/libgetenv.so /usr/lib
~~~~
####5. TelosB mote 접근 권한 변경
~~~~
$ motelist
$ sudo chmod 777 /dev/ttyUSB0
~~~~
####6. TelosB Serial 출력 확인
~~~~
$ ./flush.sh
~~~~
####참고 주소
- <https://zhongcs.wordpress.com/2015/02/15/install-tinyos-on-raspbian/>
- <http://tinyos.stanford.edu/tinyos-wiki/index.php/Manual_installation_using_RPM_packages#Step_5:_Install_the_TinyOS_2.x_source_tree>
- <http://tinyos.stanford.edu/tinyos-wiki/index.php/Installing_From_Source>