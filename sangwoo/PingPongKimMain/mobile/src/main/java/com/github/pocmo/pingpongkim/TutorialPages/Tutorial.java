package com.github.pocmo.pingpongkim.TutorialPages;

/**
 * Created by garyNoh on 2018. 3. 13..
 */

/**
 * Tutorial1,2,3 이 공통으로 구현해야하는 콜백함수 인터페이스
 *
 * *** 인터페이스를 활용해 같은 기능을 가지고 있는 여러 개 클래스를 만들 필요없어짐
 */
public class Tutorial extends android.support.v4.app.Fragment{
    public interface TutorialNextPage{
        void onMoveNextPage(int num);
    }
}
