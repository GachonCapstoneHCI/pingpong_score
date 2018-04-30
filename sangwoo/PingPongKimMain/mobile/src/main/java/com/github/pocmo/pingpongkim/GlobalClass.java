package com.github.pocmo.pingpongkim;

import java.util.ArrayList;

/**
 * Created by mhealth on 2018-03-26.
 * GlobalClass 는 전 클래스에서 공통으로 접근할 수 있는 정적 변수들을 갖고 있음
 */

public class GlobalClass {
    public static String TAG = "PINGPONGKIM"; //로그 태그
    public static int counter = 0;  //@TEST wifi direct 테스트용 counter
    public static boolean isPlaying = true; //현재 경기 중인지 여부
    public static boolean isServer; //wifi direct 에서 client 인지 server 인지 여부
    public static int serverPort, myPort;   //메시지를 전송할 포트 번호와 메시지를 받을 포트 번호
    public static String serverIP, myIP;    //메시지를 전송할 서버 IP 번호와 메시지를 받을 나의 IP 번호
    public static boolean isServe = false;  //서브 차례 여부
    public static boolean isFirst = true;
    public static String playerType = "";
    public static ArrayList<String> expectedList;
}
