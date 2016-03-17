package com.pictureair.photopass.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.util.PictureAirLog;

/**
 * 自定义对话框的封装
 * @author bauer_bao
 *
 */
public class PictureWorksDialog {
	private Context context;
	private String titleString, messageString, cancelString, okString;
	private CustomDialog customDialog;
	private Handler handler;
	private boolean gravity;

	/**
	 * 初始化
	 * 怎么使用？
	 * 1. 申明变量 PictureWorksDialog pictureWorksDialog;
	 * 2. pictureWorksDialog = new PictureWorksDialog(context, titleString, messageString, cancelString, okString, true, handler);
	 * 3. pictureWorksDialog.show();
	 * 4. 处理handler中 DialogInterface.BUTTON_POSITIVE 和 DialogInterface.BUTTON_NEGATIVE 事件
	 *
	 * @param context
	 * @param titleString 提示的title,如果不需要title，则为null
	 * @param messageString 提示的内容
	 * @param cancelString 取消按钮的文字, 设置按钮的内容，如果内容为null，则不显示按钮
	 * @param okString 确定按钮的文字, 设置按钮的内容，如果内容为null，则不显示按钮
	 * @param gravity 内容文字居中属性，居中为true，靠左为false
	 * @param handler
	 */
	public PictureWorksDialog(Context context, String titleString, String messageString, String cancelString, String okString, boolean gravity, Handler handler) {
		this.context = context;
		this.titleString = titleString;
		this.messageString = messageString;
		this.cancelString = cancelString;
		this.okString = okString;
		this.gravity = gravity;
		this.handler = handler;
		init();
	}

	private void init() {
		//初始化dialog
		customDialog = new CustomDialog.Builder(context)
		.setTitle(titleString)
		.setMessage(messageString)
		.setGravity(gravity)
		.setNegativeButton(cancelString, new DialogOnClickListener())
		.setPositiveButton(okString, new DialogOnClickListener())
		.setCancelable(false)
		.create();
	}


	/**
	 * 带输入框的
	 * 注意：带输入框的按钮监听使用接口回调的方式
	 *
	 * @param context
	 * @param titleString 提示的title,如果不需要title，则为null
	 * @param cancelString 取消按钮的文字, 设置按钮的内容，如果内容为null，则不显示按钮
	 * @param okString 确定按钮的文字, 设置按钮的内容，如果内容为null，则不显示按钮
	 * @param isEdittext  是否要输入框？
	 * @param isEdittext  输入框字符串最少长度
	 * @param edittextHint Hint
	 * @param  inputType 默认 －1，输入框的inputType属性
	 */
	public PictureWorksDialog(Context context, String titleString, String cancelString, String okString, boolean isEdittext,int lenght,String edittextHint,int inputType,CustomDialog.MyEditTextDialogInterface metdi) {
		//初始化dialog
		customDialog = new CustomDialog.Builder(context)
				.setTitle(titleString)
				.setMessage(null)
				.setGravity(false)
				.setEditText(isEdittext)
				.setEditTextHint(edittextHint)
				.setEditTextLenght(lenght)
				.setEditTextButtonClickListener(okString, cancelString,metdi)
				.setCancelable(false)
				.setEditTextInputType(inputType)
				.create();
	}

	/**
	 * 显示dialog
	 */
	public void show() {
		customDialog.show();
	}

	/**
	 * 对话框按钮的监听
	 * @author bauer_bao
	 *
	 */
	private class DialogOnClickListener implements DialogInterface.OnClickListener{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				PictureAirLog.out("click ok");
				handler.sendEmptyMessage(DialogInterface.BUTTON_POSITIVE);
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				PictureAirLog.out("click no");
				handler.sendEmptyMessage(DialogInterface.BUTTON_NEGATIVE);
				break;

			default:
				break;
			}
			dialog.dismiss();
		}
	}

}
