package com.pictureair.photopass.customDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.ScreenUtil;

/**
 * 自定义的dialog，可以替换message显示部分的布局
 * @author bauer_bao
 *
 */
public class CustomDialog extends Dialog {

	/**
	 * tips 弹窗布局。回调接口。
	 * @param context
	 * @param msg
	 * @param noMsg
	 * @param yesMsg
	 * @param myDialogInterface
	 */
	public CustomDialog(Context context, int msg, int noMsg,
						int yesMsg, final MyDialogInterface myDialogInterface) {
		super(context);
		new CustomDialog.Builder(context)
				.setMessage(context.getResources().getString(msg))
				.setNegativeButton(context.getResources().getString(noMsg),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								myDialogInterface.no();
							}
						})
				.setPositiveButton(context.getResources().getString(yesMsg),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								myDialogInterface.yes();
							}
						}).setCancelable(false).create().show();
	}
	
	public CustomDialog(Context context) {
		super(context);
	}
	
	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}
	
	public static class Builder{
		private Context mContext;
		private View contentView;
		private String title;
		private String message;
		private String positiveButtonText;
		private String negativeButtonText;
		private DialogInterface.OnClickListener positiveButtonClickListener;
		private DialogInterface.OnClickListener negativeButtonClickListener;
		private boolean cancelable = true;
		private boolean textCenter = true;
		
		public Builder(Context context) {
			this.mContext = context;
		}
		
		public Builder(Context context, String title) {
			this.mContext = context;
			this.title = title;
		}
		
		public Builder(Context context, String title, String message) {
			this.mContext = context;
			this.title = title;
			this.message = message;
		}
		
		//设置title
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}
		
		//设置message
		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}
		
		/**
		 * 设置文字居中属性， 默认居中
		 * @param gravity
		 * @return
		 */
		public Builder setGravity(boolean gravity){
			textCenter = gravity;
			return this;
		}
		
		//设置确认按钮
		public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener positiveButtonClickListener) {
			this.positiveButtonText = positiveButtonText;
			this.positiveButtonClickListener = positiveButtonClickListener == null ? new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			} : positiveButtonClickListener;
			return this;
		}
		
		//设置取消按钮
		public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener negativeButtonClickListener) {
			this.negativeButtonText = negativeButtonText;
			this.negativeButtonClickListener = negativeButtonClickListener == null ? new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			} : negativeButtonClickListener;
			return this;
		}
		
		//设置是否可以取消
		public Builder setCancelable(boolean cancelable) {
			this.cancelable = cancelable;
			return this;
		}
		
		//设置新的布局
		public Builder setContentView(View contentView) {
			this.contentView = contentView;
			return this;
		}
		
		//设置新的布局
		public Builder setContentView(int layoutResId) {
			this.contentView = View.inflate(mContext, layoutResId, null);
			return this;
		}
		
		//创建新的dialog
		public CustomDialog create() {
			final CustomDialog dialog = new CustomDialog(mContext, R.style.Dialog);
			
			View layout = View.inflate(mContext, R.layout.layout_custom_dialog, null);
			dialog.addContentView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			
			//设置title属性。我们这里用不到这个属性
//			if (title != null) {
//				TextView tvTitle = (TextView) layout.findViewById(R.id.tv_title);
//				tvTitle.setText(title);
//			}
			
			if (message != null) {
				TextView tvMessage = (TextView) layout.findViewById(R.id.tv_message);
				tvMessage.setText(message);
				tvMessage.setGravity(textCenter ? Gravity.CENTER : Gravity.START | Gravity.CENTER_VERTICAL);
			}
			
			final GroupButton btnGroup = (GroupButton) layout.findViewById(R.id.btn_group);
			{
				btnGroup.setButtonText(positiveButtonText, negativeButtonText);
				btnGroup.setOnClickListener(new GroupButton.OnClickListener() {
					@Override
					public void onPositiveButtonClicked() {
						positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
					}
					
					@Override
					public void onNegativeButtonClicked() {
						negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
					}
				});
			}
			
			//判断是否有自定义布局
			if (contentView != null) {
				FrameLayout contentViewParent = (FrameLayout) layout.findViewById(R.id.content_view);
				contentViewParent.removeAllViews();
				contentViewParent.addView(this.contentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}	
			
			dialog.setContentView(layout);
			dialog.setCancelable(cancelable);
			
			final Window dialogWindow = dialog.getWindow();
			final WindowManager.LayoutParams params = dialogWindow.getAttributes();
			params.width = (int) (ScreenUtil.getScreenWidth(mContext) * 0.9);
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			dialogWindow.setAttributes(params);
			return dialog;
		}
	}

	// 接口
	public interface MyDialogInterface{
		public void no();
		public void yes();
	}
	
}
