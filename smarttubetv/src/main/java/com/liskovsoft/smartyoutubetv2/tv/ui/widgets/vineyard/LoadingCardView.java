package com.liskovsoft.smartyoutubetv2.tv.ui.widgets.vineyard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.leanback.widget.BaseCardView;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.video.GridFragmentHelper;

public class LoadingCardView extends BaseCardView {
    private View mSkeletonRoot;
    private View mThumbnailView;
    private Animation mShimmerAnimation;

    public LoadingCardView(Context context, int styleResId) {
        super(new ContextThemeWrapper(context, styleResId), null, 0);
        buildLoadingCardView();
    }

    public LoadingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buildLoadingCardView();
    }

    private void buildLoadingCardView() {
        setFocusable(false);
        setFocusableInTouchMode(false);
        setCardType(CARD_TYPE_MAIN_ONLY);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_loading_card, this);
        mSkeletonRoot = view.findViewById(R.id.skeleton_root);
        mThumbnailView = view.findViewById(R.id.skeleton_thumbnail);

        mShimmerAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.skeleton_shimmer);
        if (mSkeletonRoot != null && mShimmerAnimation != null) {
            mSkeletonRoot.startAnimation(mShimmerAnimation);
        }
        updateDimensions();
    }

    private void updateDimensions() {
        if (mThumbnailView != null) {
            int[] dimens = GridFragmentHelper.getCardDimensPx(getContext());
            if (dimens != null && dimens.length >= 2 && dimens[0] > 0 && dimens[1] > 0) {
                mThumbnailView.getLayoutParams().width = dimens[0];
                mThumbnailView.getLayoutParams().height = dimens[1];
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mSkeletonRoot != null) {
            mSkeletonRoot.clearAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mSkeletonRoot != null && mShimmerAnimation != null) {
            mSkeletonRoot.startAnimation(mShimmerAnimation);
        }
    }
}