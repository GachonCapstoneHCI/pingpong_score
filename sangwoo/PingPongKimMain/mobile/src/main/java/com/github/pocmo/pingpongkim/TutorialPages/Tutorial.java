package com.github.pocmo.pingpongkim.TutorialPages;

/**
 * Created by garyNoh on 2018. 3. 13..
 */

/**
 * Tutorial1,2,3 이 공통으로 구현해야하는 콜백함수 인터페이스
 */
public class Tutorial extends android.support.v4.app.Fragment{
    public interface TutorialNextPage{
        void onMoveNextPage(int num);
    }
}
