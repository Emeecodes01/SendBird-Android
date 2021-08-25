package com.ulesson.chat.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ulesson.chat.R;

import java.util.HashMap;


public class ImageUtils {

    static String THEME_MATH = "mathematics_english";
    static String THEME_PHYSICS = "physics_english";
    static String THEME_CHEMISTRY = "chemistry_english";
    static String THEME_BIOLOGY = "biology_english";
    static String THEME_MATH_JS = "mathematics_english_jss";
    static String THEME_BASIC_TECHNOLOGY = "basic_technology_english";
    static String THEME_BASIC_SCIENCE = "basic_science_english";
    static String THEME_BUSINESS_STUDIES = "business_studies_english";
    static String THEME_ENGLISH = "english_english";
    static String THEME_ENGLISH_JSS = "english_english_jss";
    static String THEME_ENGLISH_PRIMARY = "primary_english_english";
    static String THEME_MATHS_PRIMARY = "primary_mathematics_english";
    static String THEME_BASIC_SCIENCE_PRIMARY = "primary_basic_science_english";

    private static HashMap<String, Theme> themeMap = new HashMap<String, Theme>();

    public static HashMap<String, Theme> getThemeMap() {

        if (themeMap.isEmpty()) {
            themeMap.put(THEME_MATH, new Theme(R.drawable.ic_maths_fill, R.drawable.ic_maths_grey_fill));
            themeMap.put(THEME_PHYSICS, new Theme(R.drawable.ic_physics_fill, R.drawable.ic_physics_grey_fill));
            themeMap.put(THEME_CHEMISTRY, new Theme(R.drawable.ic_chemistry_fill, R.drawable.ic_chemistry_grey_fill));
            themeMap.put(THEME_BIOLOGY, new Theme(R.drawable.ic_biology_fill, R.drawable.ic_biology_grey_fill));
            themeMap.put(THEME_MATH_JS, new Theme(R.drawable.ic_maths_js_fill, R.drawable.ic_maths_js_grey_fill));
            themeMap.put(THEME_BASIC_TECHNOLOGY, new Theme(R.drawable.ic_basic_tech_fill, R.drawable.ic_basic_tech_grey_fill));
            themeMap.put(THEME_BASIC_SCIENCE, new Theme(R.drawable.ic_basic_science_fill, R.drawable.ic_basic_science_grey_fill));
            themeMap.put(THEME_BUSINESS_STUDIES, new Theme(R.drawable.ic_business_studies_fill, R.drawable.ic_business_studies_grey_fill));
            themeMap.put(THEME_ENGLISH, new Theme(R.drawable.ic_english_fill, R.drawable.ic_english_grey_fill));
            themeMap.put(THEME_ENGLISH_JSS, new Theme(R.drawable.ic_english_fill, R.drawable.ic_english_grey_fill));
            themeMap.put(THEME_ENGLISH_PRIMARY, new Theme(R.drawable.ic_english_primary_fill, R.drawable.ic_english_primary_grey_fill));
            themeMap.put(THEME_MATHS_PRIMARY, new Theme(R.drawable.ic_maths_primary_fill, R.drawable.ic_maths_primary_grey_fill));
            themeMap.put(THEME_BASIC_SCIENCE_PRIMARY, new Theme(R.drawable.ic_basic_science_primary_fill, R.drawable.ic_basic_science_primary_grey_fill));

        }

        return themeMap;
    }

    public static class Theme {
        public int activeIcon;
        public int pastIcon;

        public Theme(int activeIcon, int pastIcon) {
            this.activeIcon = activeIcon;
            this.pastIcon = pastIcon;
        }

    }

    // Prevent instantiation
    private ImageUtils() {

    }

    /**
     * Crops image into a circle that fits within the ImageView.
     */
    public static void displayRoundImageFromUrl(final Context context, final String url, final ImageView imageView) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate();

        Glide.with(context)
                .asBitmap()
                .apply(myOptions)
                .load(url)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }


    /**
     * Crops image into a circle that fits within the ImageView.
     */
    public static void displayRoundCornerImageFromUrl(final Context context, final String url, final ImageView imageView) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(context)
                .asBitmap()
                .apply(myOptions)
                .load(url)
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCornerRadius(38);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void displayImageFromUrl(final Context context, final String url,
                                           final ImageView imageView) {
        displayImageFromUrl(context, url, imageView, null);
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    public static void displayImageFromUrl(final Context context, final String url,
                                           final ImageView imageView, RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        if (listener != null) {
            Glide.with(context)
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        }
    }

    public static void displayRoundImageFromUrlWithoutCache(final Context context, final String url,
                                                            final ImageView imageView) {
        displayRoundImageFromUrlWithoutCache(context, url, imageView, null);
    }

    public static void displayRoundImageFromUrlWithoutCache(final Context context, final String url,
                                                            final ImageView imageView, RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true);

        if (listener != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .apply(myOptions)
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            imageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
    }

    /**
     * Displays an image from a URL in an ImageView.
     * If the image is loading or nonexistent, displays the specified placeholder image instead.
     */
    public static void displayImageFromUrlWithPlaceHolder(final Context context, final String url,
                                                          final ImageView imageView,
                                                          int placeholderResId) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(placeholderResId);

        Glide.with(context)
                .load(url)
                .apply(myOptions)
                .into(imageView);
    }

    /**
     * Displays an image from a URL in an ImageView.
     */
    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView, RequestListener listener) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        if (listener != null) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .listener(listener)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .into(imageView);
        }
    }

    /**
     * Displays an GIF image from a URL in an ImageView.
     */
    public static void displayGifImageFromUrl(Context context, String url, ImageView imageView, String thumbnailUrl) {
        RequestOptions myOptions = new RequestOptions()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        if (thumbnailUrl != null) {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .thumbnail(Glide.with(context).asGif().load(thumbnailUrl))
                    .into(imageView);
        } else {
            Glide.with(context)
                    .asGif()
                    .load(url)
                    .apply(myOptions)
                    .into(imageView);
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Bitmap bitmap = null;
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }
}
