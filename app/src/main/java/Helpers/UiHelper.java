package helpers;

import com.holemcross.deltatrack.R;
import com.holemcross.deltatrack.data.CtaRoutes;

/**
 * Created by amortega on 9/2/2016.
 */
public class UiHelper {
    public static class Colors{
       public static int getRouteBackgroundColorByCtaRoute(CtaRoutes route){
           switch (route){
               case Blue:
                   return R.color.dashRowTrainBlue;
               case Brown:
                   return R.color.dashRowTrainBrown;
               case Green:
                   return R.color.dashRowTrainGreen;
               case Orange:
                   return R.color.dashRowTrainOrange;
               case Pink:
                   return R.color.dashRowTrainPink;
               case Purple:
                   return R.color.dashRowTrainPurple;
               case Red:
                   return R.color.dashRowTrainRed;
               case Yellow:
                   return R.color.dashRowTrainYellow;
               default:
                   return 0;
           }
       }
        public static int getTextColorByCtaRoute(CtaRoutes route){
            switch (route){
                case Yellow:
                    return R.color.dashRowTextDark;
                default:
                    return R.color.dashRowText;
            }
        }
    }
}
