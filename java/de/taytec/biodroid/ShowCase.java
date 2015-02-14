package de.taytec.biodroid;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;

import java.util.ArrayList;

public class ShowCase implements OnShowcaseEventListener {
    private final BioDroidActivity bioDroidActivity;
    final ArrayList<ShowCaseElement> list = new ArrayList<ShowCaseElement>(5);
    private int viewIndex;
    private final ShowcaseView.Builder showcaseView;

    public ShowCase(BioDroidActivity bioDroidActivity) {
        this.bioDroidActivity = bioDroidActivity;
        showcaseView = new ShowcaseView.Builder(bioDroidActivity, true)
                .hideOnTouchOutside()
                .setShowcaseEventListener(this);
    }

    void showDemo() {
        viewIndex = 0;
        ShowCaseElement currentElement = list.get(viewIndex);
        showcaseView
                .setTarget(currentElement.target)
                .setContentTitle((viewIndex + 1) + "/" + list.size() + " – " + currentElement.title)
                .setContentText(currentElement.text)
                .setStyle(R.style.ShowCaseBio)
                .build();
    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
        viewIndex++;
        if (viewIndex < list.size()) {
            ShowCaseElement currentElement = list.get(viewIndex);
            showcaseView.setTarget(currentElement.target);
            showcaseView.setContentTitle((viewIndex + 1) + "/" + list.size() + " – " + currentElement.title);
            showcaseView.setContentText(currentElement.text);
            showcaseView.show();
        }
    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {

    }

    public void addCase(ShowCaseElement el) {
        list.add(el);
    }

    public void addCase(Target target, int titleId, int textId) {
        ShowCaseElement el = new ShowCaseElement();
        el.target = target;
        el.title = bioDroidActivity.getString(titleId);
        el.text = bioDroidActivity.getString(textId);
        addCase(el);
    }


    private class ShowCaseElement {
        Target target;
        String title;
        String text;
    }
}