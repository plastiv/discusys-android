package com.tohoku.discussionboard.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.tohoku.discussionboard.R;

public class BadgeView extends View {

	private Bitmap mImage;
	private final DirectionLock mLock = DirectionLock.FREE;
	private final Paint mPaint = new Paint();
	private final Region mRegion = new Region();
	private final Point mSize = new Point();
	private final Point mStartPosition = new Point();

	public BadgeView(Context context) {

		super(context);
		setDefaultDrawable();
	}

	public BadgeView(Context context, AttributeSet attrs) {

		super(context, attrs);
		setDefaultDrawable();
	}

	public BadgeView(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);
		setDefaultDrawable();
	}

	public final Bitmap getImage() {

		return mImage;
	}

	public final Point getPosition() {

		Rect bounds = mRegion.getBounds();
		return new Point(bounds.left, bounds.top);
	}

	public final Point getSize() {

		return mSize;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// Is the event inside of this view?
		if (!mRegion.contains((int) event.getX(), (int) event.getY())) {
			return super.onTouchEvent(event);
		}
		int eventAction = event.getAction();
		switch (eventAction) {
			case MotionEvent.ACTION_DOWN: {
				mStartPosition.x = (int) event.getX();
				mStartPosition.y = (int) event.getY();
				bringToFront();
				return true;
			}
			case MotionEvent.ACTION_MOVE: {
				int x = 0, y = 0;
				if ((mLock == DirectionLock.FREE) || (mLock == DirectionLock.HORIZONTAL_ONLY)) {
					x = (int) event.getX() - mStartPosition.x;
				}
				if ((mLock == DirectionLock.FREE) || (mLock == DirectionLock.VERTICAL_ONLY)) {
					y = (int) event.getY() - mStartPosition.y;
				}
				mRegion.translate(x, y);
				mStartPosition.x = (int) event.getX();
				mStartPosition.y = (int) event.getY();
				invalidate();
				return true;
			}
			default:
				return super.onTouchEvent(event);
		}
	}

	public final void setImage(Bitmap image) {

		mImage = image;
		setSize(mImage.getWidth(), mImage.getHeight());
	}

	public final void setPosition(final Point position) {

		mRegion.set(position.x, position.y, position.x + mSize.x, position.y + mSize.y);
	}

	public final void setSize(int width, int height) {

		mSize.x = width;
		mSize.y = height;
		Rect bounds = mRegion.getBounds();
		mRegion.set(bounds.left, bounds.top, bounds.left + width, bounds.top + height);
	}

	private void setDefaultDrawable() {

		Resources res = getResources();
		setImage(BitmapFactory.decodeResource(res, R.drawable.photo2));
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Point position = getPosition();
		canvas.drawBitmap(mImage, position.x, position.y, mPaint);
	}

	enum DirectionLock {
		FREE, HORIZONTAL_ONLY, VERTICAL_ONLY;
	}
}