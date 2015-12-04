package com.pictureair.photopass.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 问题及答案
 * 实现序列化
 */

public class QuestionInfo implements Parcelable{
	
	public String questionId;//问题id
	public String questionName;//问题名称
	public String answer;//答案
	
	//读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。因为实现类在这里还是不可知的，所以需要用到模板的方式，继承类名通过模板参数传入
	//为了能够实现模板参数的传入，这里定义Creator嵌入接口,内含两个接口函数分别返回单个和多个继承类实例
	public static final Parcelable.Creator<QuestionInfo> CREATOR = new Creator<QuestionInfo>(){

		@Override
		public QuestionInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new QuestionInfo(source);
		}

		@Override
		public QuestionInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new QuestionInfo[size];
		}
		
	};
	
	public QuestionInfo() {

	}
	
	public QuestionInfo(Parcel source) {
		questionId=source.readString();
		questionName=source.readString();
		answer=source.readString();
	}
	
	//内容描述接口
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	//写入接口函数，打包
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(questionId);
		dest.writeString(questionName);
		dest.writeString(answer);
	}

}
