package helpers;

import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.data.CtaRoutes;

/**
 * Created by amortega on 9/2/2016.
 */
public class UiHelper {
    public static class Colors{
       public static int getRouteBackgroundColorByCtaRoute(CtaRoutes route){
           int result = 0;
           switch (route){
               case Blue:
                   result = R.color.dashRowTrainBlue;
                   break;
               case Brown:
                   result =  R.color.dashRowTrainBrown;
                   break;
               case Green:
                   result =  R.color.dashRowTrainGreen;
                   break;
               case Orange:
                   result =  R.color.dashRowTrainOrange;
                   break;
               case Pink:
                   result =  R.color.dashRowTrainPink;
                   break;
               case Purple:
                   result =  R.color.dashRowTrainPurple;
                   break;
               case Red:
                   result =  R.color.dashRowTrainRed;
                   break;
               case Yellow:
                   result =  R.color.dashRowTrainYellow;
                   break;
           }
           return result;
       }
        public static int getTextColorByCtaRoute(CtaRoutes route){
            int result = R.color.dashRowText;
            switch (route){
                case Yellow:
                    result = R.color.dashRowTextDark;
                    break;
            }
            return result;
        }
    }
}
