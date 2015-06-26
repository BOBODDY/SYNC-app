package com.sync.syncapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.sync.syncapp.activities.MainActivity;

import java.util.List;

/**
 * Created by nick on 6/26/15.
 *
 * My custom Navigation Drawer layout
 */
public class NavDrawer extends View {

    ImageView profile;
    ListView items;

    public NavDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init() {
        profile = (ImageView) findViewById(R.id.nav_drawer_picture);

        items = (ListView) findViewById(R.id.nav_drawer_items);
    }

    public void setAdapter(ListAdapter adapter) {
        items.setAdapter(adapter);
    }

    public void setOnItemClickListener(MainActivity.DrawerItemClickListener drawerListener) {
        items.setOnItemClickListener(drawerListener);
    }

    public void setProfileImage(Bitmap bmp) {
        profile.setImageBitmap(bmp);
    }
}
