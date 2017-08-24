package com.pictureair.hkdlphotopass.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.pictureair.hkdlphotopass.R;

/**
 * gif播放页面，目前没有用到了
 * Created by bauer_bao on 16/12/8.
 */

public class GifPlayActivity extends BaseActivity {
    private ImageView gifIV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gifIV = new ImageView(this);
        setContentView(gifIV);
//        GlideUtil.loadGif(this, GlideUtil.getDrawableUrl(this, R.drawable.story_pp_intro), gifIV);
        gifIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
            }
        });
    }
}
