package util;

import android.view.View;
import android.view.View.OnClickListener;

public class onClickListenerWithParam implements OnClickListener
   {

     protected int onclickedStopNum;
     public onClickListenerWithParam(int onclickedStopNum) {
          this.onclickedStopNum = onclickedStopNum;
     }

     @Override
     public void onClick(View v)
     {
         //read your lovely variable
     }

  };