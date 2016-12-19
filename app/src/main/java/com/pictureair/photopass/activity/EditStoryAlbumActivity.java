package com.pictureair.photopass.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.StickyRecycleAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.JsonInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.greendao.RefreshAndLoadMoreCallBack;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.SettingUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.PWStickySectionRecyclerView;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.SharePop;

import java.util.ArrayList;
import java.util.Iterator;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * pp照片详情页面
 * @author bauer_bao
 *
 */
public class EditStoryAlbumActivity extends BaseActivity implements OnClickListener, PWDialog.OnPWDialogClickListener, PWStickySectionRecyclerView.OnPullListener{
	private ImageView backRelativeLayout, editImageView;
	private TextView selectAllTextView, disAllTextView, downloadTextView, deleteTextView, shareTextView, titleTextView;
	private LinearLayout editBarLinearLayout;
	private PWStickySectionRecyclerView pwStickySectionRecyclerView;
	private RelativeLayout noCountView;
	private TextView noCountTextView;
	private SwipeRefreshLayout refreshLayout;
	private RelativeLayout buyPPPRl;
	private TextView buyPPPTv;
	private TextView ppCardTv, ppTimeTv;

	private ArrayList<PhotoInfo> albumArrayList;
	private ArrayList<PhotoInfo> photopassPhotoslist = new ArrayList<>();//选择的网络图片的list
	private ArrayList<DiscoverLocationItemInfo> locationList = new ArrayList<>();

	private final static int GET_PHOTOS_DONE = 13;
	private final static int START_DELETE_NETWORK_PHOTOS = 14;
	private final static int DELETE_DIALOG = 16;
	private static final int GO_SETTING_DIALOG = 17;
	private static final int DOWNLOAD_DIALOG = 18;
	private static final int HAS_UNPAY_PHOTOS_DIALOG = 19;
	private static final int GO_DOWNLOAD_ACTIVITY_DIALOG = 20;
	private static final int HAS_ALL_UNPAY_PHOTOS_DIALOG = 21;
	private static final int FIRST_TEN_PHOTOS_TIP_DIALOG = 22;

	private int selectCount = 0;
	private PWToast myToast;
	private SettingUtil settingUtil;
	private boolean editMode = false;
	private boolean netWorkFailed = false;
	private String ppCode;
	private String shootDate;
	private int activated;
	private int photoCount;
	private int newCount;
	private PWDialog pictureWorksDialog;
	private SharePop sharePop;
	private int shareType = 0;
	private int prepareDownloadCount;
	private String userId;
	private int oldCount = 0;
	private int ppPhotoCount, refreshCount, loadMoreCount;
	private String ppRefreshTime, ppRefreshIds, ppLoadMoreTime, ppLoadMoreIds;

	private Handler editStoryAlbumHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case SharePop.TWITTER:
					shareType = msg.what;
					break;

				case SharePop.DISMISS_DIALOG:
					dismissPWProgressDialog();
					break;

				case SharePop.SHOW_DIALOG:
					showPWProgressDialog(null);
					break;

				case GET_PHOTOS_DONE://获取图片成功
					dismissPWProgressDialog();
					if (albumArrayList.size() == 0){
						noCountView.setVisibility(View.VISIBLE);
						noCountTextView.setText(R.string.no_photo_in_airpass);
					}
					break;

				case START_DELETE_NETWORK_PHOTOS://开始删除网络图片
					JSONArray ids = new JSONArray();
					for (int i = 0; i < photopassPhotoslist.size(); i++) {
						ids.add(photopassPhotoslist.get(i).getPhotoId());
					}
					PictureAirLog.out("ids---->" + ids);
					PictureAirLog.out("ppCode---->" + ppCode);
					API1.removePhotosFromPP(MyApplication.getTokenId(), ids, ppCode, editStoryAlbumHandler);
					break;

				case API1.DELETE_PHOTOS_FAILED://判断本地图片是否删除完毕，并且更具有没有本地图片而显示不同的提示
					//需要处理
					netWorkFailed = true;
					dealAfterDeleted();
					break;

				case API1.DELETE_PHOTOS_SUCCESS://判断本地图片是否删除完毕
					/**
					 * 1.删除列表内的数据
					 * 2.判断本地数据是否处理完毕
					 */
					netWorkFailed = false;
					//删除本地列表数据操作
					deleteNetworkPhotos();
					selectCount -= photopassPhotoslist.size();
					SPUtils.put(EditStoryAlbumActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.IS_DELETED_PHOTO_FROM_PP, true);
					dealAfterDeleted();
					break;

				case API1.GET_PPP_SUCCESS:
					if (ppPhotoCount >= 10 && API1.PPPlist.size() == 0) {
						pictureWorksDialog.setPWDialogId(FIRST_TEN_PHOTOS_TIP_DIALOG)
								.setPWDialogMessage(R.string.pp_first_up10_msg)
								.setPWDialogNegativeButton(R.string.pp_first_up10_no_msg)
								.setPWDialogPositiveButton(R.string.pp_first_up10_yes_msg)
								.pwDilogShow();
						settingUtil.insertSettingFirstPP10Status(userId);
					} else if (API1.PPPlist.size() > 0) {
						settingUtil.insertSettingFirstPP10Status(userId);
					}
					break;

				default:
					break;
			}
			return false;
		}
	});

	/**
	 * 开始下载
	 * @param hasUnPayPhotos 是否有未购买的图片
     */
	private void startDownload(boolean hasUnPayPhotos){
		ArrayList<PhotoInfo> hasPayedList = new ArrayList<>();
		Intent intent = new Intent(EditStoryAlbumActivity.this, DownloadService.class);
		//将已购买并且已选择的加入下载队列中
		for (int i = 0; i < albumArrayList.size(); i++) {
			if (albumArrayList.get(i).getIsSelected() == 1) {
				if (hasUnPayPhotos) {
					if (albumArrayList.get(i).getIsPaid() == 1) {
						hasPayedList.add(albumArrayList.get(i));
					}
				} else {
					hasPayedList.add(albumArrayList.get(i));
				}
			}

			if (i != 0 && (i % 50 == 0) && (i != albumArrayList.size() - 1) && hasPayedList.size() > 0) {//如果全部扔过去，超出intent传递的限制，报错，因此分批扔过去，每次扔50个
				//开始将图片加入下载队列
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("photos", hasPayedList);
				bundle.putInt("prepareDownloadCount",prepareDownloadCount);
				intent.putExtras(bundle);
				startService(intent);
				hasPayedList.clear();
			}
		}

		PictureAirLog.out("download list size---->" + hasPayedList.size());

		if (hasPayedList.size() > 0) {
			//开始将图片加入下载队列
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("photos", hasPayedList);
			bundle.putInt("prepareDownloadCount", prepareDownloadCount);
			intent.putExtras(bundle);
			startService(intent);
		}

		//弹框提示，可以进去下载管理页面
		pictureWorksDialog.setPWDialogId(GO_DOWNLOAD_ACTIVITY_DIALOG)
				.setPWDialogMessage(R.string.edit_story_addto_downloadlist)
				.setPWDialogNegativeButton(null)
				.setPWDialogPositiveButton(R.string.reset_pwd_ok)
				.pwDilogShow();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_story_photo);

		//find控件
		backRelativeLayout = (ImageView) findViewById(R.id.rlrt);
		deleteTextView = (TextView) findViewById(R.id.select_delete);
		selectAllTextView = (TextView) findViewById(R.id.select_all);
		disAllTextView = (TextView) findViewById(R.id.select_disall);
		downloadTextView = (TextView) findViewById(R.id.select_download);
		shareTextView = (TextView) findViewById(R.id.select_share);
		editBarLinearLayout = (LinearLayout) findViewById(R.id.select_tools_linearlayout);
		pwStickySectionRecyclerView = (PWStickySectionRecyclerView) findViewById(R.id.pullToRefreshPinnedSectionListView);
		titleTextView = (TextView) findViewById(R.id.text);
		noCountView = (RelativeLayout) findViewById(R.id.no_photo_relativelayout);
		noCountTextView = (TextView) findViewById(R.id.no_photo_textView);
		editImageView = (ImageView) findViewById(R.id.pp_photos_edit);
		buyPPPRl = (RelativeLayout) findViewById(R.id.buy_ppp_rl);
		buyPPPTv = (TextView) findViewById(R.id.edit_album_buy_tv);
		ppCardTv = (TextView) findViewById(R.id.edit_album_card_no_tv);
		ppTimeTv = (TextView) findViewById(R.id.edit_album_time_tv);

		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
		refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
		refreshLayout.setEnabled(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				PictureAirLog.out("start refresh");
				if (albumArrayList.size() != 0 && !pwStickySectionRecyclerView.isLoadMore() && !editMode) {//有数量，不在加载更多，也没有编辑状态
					getPhotosFromNetWork(API1.GET_NEW_PHOTOS, ppRefreshTime, ppRefreshIds);
				}
			}
		});

		//绑定监听
		backRelativeLayout.setOnClickListener(this);
		deleteTextView.setOnClickListener(this);
		deleteTextView.setEnabled(false);
		selectAllTextView.setOnClickListener(this);
		disAllTextView.setOnClickListener(this);
		downloadTextView.setOnClickListener(this);
		downloadTextView.setEnabled(false);
		shareTextView.setOnClickListener(this);
		shareTextView.setEnabled(false);
		editImageView.setOnClickListener(this);
		buyPPPTv.setOnClickListener(this);

		//初始化数据
		sharePop = new SharePop(this);
		albumArrayList = new ArrayList<>();
		settingUtil = new SettingUtil();
		ppCode = getIntent().getStringExtra("ppCode");
		shootDate = getIntent().getStringExtra("shootDate");
		activated = getIntent().getIntExtra("activated", 0);
		photoCount = getIntent().getIntExtra("photoCount", 0);

		if (activated == 1) {
			buyPPPRl.setVisibility(View.GONE);
		} else {
			buyPPPRl.setVisibility(View.VISIBLE);
		}

		titleTextView.setText(String.format(getString(R.string.edit_story_photo_title), photoCount));
		ppCardTv.setText(String.format(getString(R.string.story_card), ppCode));
		ppTimeTv.setText(shootDate);

		userId = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");

		locationList.addAll(AppUtil.getLocation(getApplicationContext(), ACache.get(getApplicationContext()).getAsString(Common.DISCOVER_LOCATION), true));
		showPWProgressDialog(R.string.is_loading);
		pictureWorksDialog = new PWDialog(this)
				.setOnPWDialogClickListener(this)
				.pwDialogCreate();

		myToast = new PWToast(this);
		pwStickySectionRecyclerView.setOnRecyclerViewItemClickListener(new StickyRecycleAdapter.OnRecyclerViewItemClickListener() {
			@Override
			public void onItemClick(StickyRecycleAdapter.RecyclerItemViewHolder view, int position) {
				if (editMode) {//编辑模式，需要选中效果
					itemOnClick(position, view);
				} else {//预览模式，点击进入大图预览
					PictureAirLog.out("select" + position);
					if (albumArrayList.get(position).getIsVideo() == 1 && albumArrayList.get(position).getIsPaid() == 0) {
						PhotoInfo info = albumArrayList.get(position);
						Intent intent = new Intent(EditStoryAlbumActivity.this, ADVideoDetailProductActivity.class);
						intent.putExtra("videoInfo", info);
						Bundle bundle = new Bundle();
						bundle.putInt("position", position);
						bundle.putString("tab", "editStory");
						intent.putExtra("bundle", bundle);
						startActivity(intent);
					} else {
						Intent i = new Intent();
						i.setClass(EditStoryAlbumActivity.this, PreviewPhotoActivity.class);
						Bundle bundle = new Bundle();
						bundle.putInt("position", position);
						bundle.putString("tab", "editStory");
						bundle.putString("ppCode", ppCode);
						bundle.putString("photoId", albumArrayList.get(position).getPhotoId());
						i.putExtra("bundle", bundle);
						startActivity(i);
					}
				}
			}

			@Override
			public void onLoadMoreClick(View view, int position) {
				loadMore();
			}
		});
		pwStickySectionRecyclerView.initDate(albumArrayList, false);
		pwStickySectionRecyclerView.setOnPullListener(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (PictureAirDbManager.isJsonInfoExist(JsonInfo.getNeedRefreshString(ppCode, shootDate))) {//需要从网络获取全部数据
					//开始从网络获取数据
					getPhotosFromNetWork(API1.GET_DEFAULT_PHOTOS, null, null);
				} else {//从数据库获取数据
					//从数据库获取数据, 并且需要设置oldCount数量
//					getFromDb;


					//判断用户是否超过10张照片
					if (settingUtil.isFirstPP10(userId)) {
						//第一次 PP数量到 10 。
						API1.getPPPSByUserId(MyApplication.getTokenId(), editStoryAlbumHandler);
					}
					//判断本地数量是否为0，如果为0，需要重新从服务器获取数据
					if (albumArrayList.size() == 0) {
						getPhotosFromNetWork(API1.GET_DEFAULT_PHOTOS, null, null);
					} else {//直接显示照片
						pwStickySectionRecyclerView.notifyDataSetChanged();
					}
				}
			}
		}).start();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sharePop != null) {
			PictureAirLog.out("sharePop not null");
			if (shareType != SharePop.TWITTER) {
				PictureAirLog.out("dismiss dialog");
				dismissPWProgressDialog();
			}
		}
	}

	/**
	 * 获取照片
	 * @param type
	 * @param receivedOn
	 * @param repeatIds
     */
	private void getPhotosFromNetWork(final int type, String receivedOn, final String repeatIds) {
		API2.getPhotosByConditions(MyApplication.getTokenId(), type, receivedOn, repeatIds, ppCode, shootDate, Common.LOAD_PHOTO_COUNT)
				.subscribeOn(Schedulers.io())
				.map(new Func1<JSONObject, ArrayList<PhotoInfo>>() {
					@Override
					public ArrayList<PhotoInfo> call(JSONObject jsonObject) {
						//删除此卡此天所有数据
						if (type == API1.GET_DEFAULT_PHOTOS) {
							PictureAirDbManager.deleteAllInfoFromTable(ppCode, shootDate);

						}
						JSONArray responseArray = jsonObject.getJSONArray("photos");

						//解析数据 并且存到数据库
						ArrayList<PhotoInfo> resultList = PictureAirDbManager.insertPhotoInfoIntoPhotoPassInfo(responseArray, type,
								MyApplication.getInstance().getLanguageType(), locationList, new RefreshAndLoadMoreCallBack() {
							@Override
							public void getRefreshData(String refreshIds, String refreshTime) {
								PictureAirLog.d("get the limit data 1");
								ppRefreshIds = refreshIds;
								ppRefreshTime = refreshTime;

							}

							@Override
							public void getLoadMoreData(String loadMoreIds, String loadMoreTime) {
								PictureAirLog.d("get the limit data 12");
								ppLoadMoreIds = loadMoreIds;
								ppLoadMoreTime = loadMoreTime;

							}

							@Override
							public void getAllData(String refreshIds, String refreshTime, String loadMoreIds, String loadMoreTime) {
								PictureAirLog.d("get the limit data 123");
								ppRefreshIds = refreshIds;
								ppRefreshTime = refreshTime;
								ppLoadMoreIds = loadMoreIds;
								ppLoadMoreTime = loadMoreTime;

							}
						});
						return AppUtil.insertSortFavouritePhotos(resultList, true);
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new RxSubscribe<ArrayList<PhotoInfo>>() {
					@Override
					public void _onNext(ArrayList<PhotoInfo> photoInfos) {
						if (type == API1.GET_NEW_PHOTOS) {
							refreshCount = photoInfos.size();
						} else if (type == API1.GET_OLD_PHOTOS) {
							loadMoreCount = photoInfos.size();
						} else {
							albumArrayList.clear();
							ppPhotoCount = photoInfos.size();
						}
						newCount = photoInfos.size();

						albumArrayList.addAll(photoInfos);
					}

					@Override
					public void _onError(int status) {
						//获取照片失败
						if (type == API1.GET_DEFAULT_PHOTOS) {
							ppRefreshIds = null;
							ppRefreshTime = null;
							ppLoadMoreIds = null;
							ppLoadMoreTime = null;
							dismissPWProgressDialog();
							//显示无网络页面



						} else if (type == API1.GET_NEW_PHOTOS) {
							PictureAirLog.d("refresh" + refreshLayout.isRefreshing());
							if (refreshLayout.isRefreshing()) {
								refreshLayout.setRefreshing(false);
							}

						} else if (type == API1.GET_OLD_PHOTOS) {
							refreshLayout.setEnabled(true);
							if (pwStickySectionRecyclerView.isLoadMore()) {
								pwStickySectionRecyclerView.setIsLoadMore(false);
								pwStickySectionRecyclerView.setLoadMoreType(StickyRecycleAdapter.LOAD_MORE_FAILED);
							}
						}
						//弹toast提示
						myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);

					}

					@Override
					public void onCompleted() {
						if (type == API1.GET_DEFAULT_PHOTOS) {//获取全部数据成功，需要删除对应的数据
							PictureAirDbManager.deleteJsonInfosByTypeAndString(JsonInfo.DAILY_PP_REFRESH_ALL_TYPE, JsonInfo.getNeedRefreshString(ppCode, shootDate));
							dismissPWProgressDialog();
						} else if (type == API1.GET_NEW_PHOTOS) {//刷新
							if (refreshLayout.isRefreshing()) {
								refreshLayout.setRefreshing(false);
							}

						} else {//加载更多
							refreshLayout.setEnabled(true);
							pwStickySectionRecyclerView.setIsLoadMore(false);
							if (newCount >= oldCount) {
								if (newCount - oldCount < Common.LOAD_PHOTO_COUNT) {//说明刷新出来的照片已经小于额定值了，说明没有更多数据了
									pwStickySectionRecyclerView.setLoadMoreType(StickyRecycleAdapter.LOAD_MORE_NO_MORE);

								}
							} else {//属于删除pp卡操作，因此如果少于一页，可以点击加载更多
								pwStickySectionRecyclerView.setLoadMoreType(StickyRecycleAdapter.LOAD_MORE_CLICK_LOAD);
							}
							oldCount = newCount;
						}
						pwStickySectionRecyclerView.notifyDataSetChanged();
					}
				});
	}

	/**
	 * 选中或取消处理
	 * @param position
     */
	private void itemOnClick(int position, StickyRecycleAdapter.RecyclerItemViewHolder viewHolder) {
		PhotoInfo info = albumArrayList.get(position);
		PictureAirLog.out("select" + position);
		//选择事件
		if (info.getIsSelected() == 1) {//取消选择
			selectCount--;
			info.setIsSelected(0);
			viewHolder.mSelectImageView.setImageResource(R.drawable.sel3);
			viewHolder.mMaskImageView.setVisibility(View.GONE);
		} else {//选择
			selectCount++;
			info.setIsSelected(1);
			viewHolder.mSelectImageView.setImageResource(R.drawable.sel2);
			viewHolder.mMaskImageView.setVisibility(View.VISIBLE);
		}
		if (selectCount == 0) {
			deleteTextView.setEnabled(false);
			downloadTextView.setEnabled(false);
		} else if (selectCount == (albumArrayList.size() - albumArrayList.get(albumArrayList.size() - 1).getSectionId() - 1)) {
			selectAllTextView.setVisibility(View.GONE);
			disAllTextView.setVisibility(View.VISIBLE);
		} else {
			selectAllTextView.setVisibility(View.VISIBLE);
			disAllTextView.setVisibility(View.GONE);
			deleteTextView.setEnabled(true);
			downloadTextView.setEnabled(true);
		}
		if (selectCount == 1) {
			shareTextView.setEnabled(true);

		} else {
			shareTextView.setEnabled(false);

		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rlrt:
				returnBack();
				break;

			case R.id.select_delete:
				if (selectCount == 0) {
					myToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
					return;
				}

				pictureWorksDialog.setPWDialogId(DELETE_DIALOG)
						.setPWDialogMessage(R.string.start_delete)
						.setPWDialogNegativeButton(R.string.button_cancel)
						.setPWDialogPositiveButton(R.string.reset_pwd_ok)
						.pwDilogShow();
				break;

			case R.id.select_all:
				selectCount = 0;
				for (int i = 1; i < albumArrayList.size(); i++) {
					if (albumArrayList.get(i).getSectionId() == albumArrayList.get(i - 1).getSectionId()) {//不是header
						albumArrayList.get(i).setIsSelected(1);
						selectCount ++;
					}
				}
				PictureAirLog.d("select all count " + selectCount);
				pwStickySectionRecyclerView.notifyDataSetChanged();
				selectAllTextView.setVisibility(View.GONE);
				disAllTextView.setVisibility(View.VISIBLE);
				deleteTextView.setEnabled(true);
				downloadTextView.setEnabled(true);
				if (selectCount == 1) {
					shareTextView.setEnabled(true);

				} else {
					shareTextView.setEnabled(false);
				}
				break;

			case R.id.select_disall:
				for (int i = 0; i < albumArrayList.size(); i++) {
					albumArrayList.get(i).setIsSelected(0);
				}
				pwStickySectionRecyclerView.notifyDataSetChanged();
				selectCount = 0;
				selectAllTextView.setVisibility(View.VISIBLE);
				disAllTextView.setVisibility(View.GONE);
				deleteTextView.setEnabled(false);
				downloadTextView.setEnabled(false);
				shareTextView.setEnabled(false);
				break;

			case R.id.select_download:
				judgeOnePhotoDownloadFlow();
				break;

			case R.id.select_share:
				PictureAirLog.d("share--->");
				for (int i = 0; i < albumArrayList.size(); i++) {
					if (albumArrayList.get(i).getIsSelected() == 1) {
						sharePop.setshareinfo(albumArrayList.get(i), editStoryAlbumHandler);
						return;
					}
				}
				sharePop.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
				break;

			case R.id.pp_photos_edit:
				if (refreshLayout.isRefreshing() || pwStickySectionRecyclerView.isLoadMore()) {//如果刷新或者加载更多的时候，不允许编辑
					return;
				}
				UmengUtil.onEvent(EditStoryAlbumActivity.this,Common.EVENT_ONCLICK_EDIT_PHOTO); //统计点 编辑时候的事件（友盟）
				editMode = true;
				editBarLinearLayout.setVisibility(View.VISIBLE);
				titleTextView.setText(R.string.edit_story_album);
				editImageView.setVisibility(View.GONE);
				if (buyPPPRl.isShown()) {
					buyPPPRl.setVisibility(View.GONE);
				}
				pwStickySectionRecyclerView.setEditMode(editMode);
				refreshLayout.setEnabled(false);
				break;

			case R.id.edit_album_buy_tv://购买ppp

				break;

			default:
				break;
		}
	}

	private void downloadPic(){
		int unPayCount = 0;
		int downloadCount =0;
		for (int i = 0; i < albumArrayList.size(); i++) {
			if (albumArrayList.get(i).getIsSelected() == 1) {
				downloadCount++;
				if (albumArrayList.get(i).getIsPaid() == 0) {
					unPayCount++;
				}
			}
		}
		if (unPayCount > 0) {//弹框提示
			prepareDownloadCount = downloadCount-unPayCount;
			if (prepareDownloadCount < 0) prepareDownloadCount = 0;
			pictureWorksDialog.setPWDialogId(unPayCount < selectCount ? HAS_UNPAY_PHOTOS_DIALOG : HAS_ALL_UNPAY_PHOTOS_DIALOG)
					.setPWDialogMessage(unPayCount < selectCount ? R.string.edit_story_unpay_tips : R.string.edit_story_all_unpay_tips)
					.setPWDialogNegativeButton(unPayCount < selectCount ? getString(R.string.edit_story_reselect) : null)
					.setPWDialogPositiveButton(unPayCount < selectCount ? R.string.edit_story_confirm_download : R.string.edit_story_reselect)
					.pwDilogShow();

		} else {
			prepareDownloadCount = downloadCount;
			if (prepareDownloadCount < 0) prepareDownloadCount = 0;
			startDownload(false);
		}
	}

	/**
	 * tips 1，网络下载流程。
	 */
	private void judgeOnePhotoDownloadFlow() { // 如果当前是wifi，无弹窗提示。如果不是wifi，则提示。
		if (!AppUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			myToast.setTextAndShow(R.string.permission_storage_message, Common.TOAST_SHORT_TIME);
			return;
		}

		if (AppUtil.getNetWorkType(EditStoryAlbumActivity.this) == AppUtil.NETWORKTYPE_WIFI) {
			downloadPic();
		} else {
			// 判断用户是否设置过 “仅wifi” 的选项。
			if (settingUtil.isOnlyWifiDownload(SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, ""))) {
				pictureWorksDialog.setPWDialogId(GO_SETTING_DIALOG)
						.setPWDialogMessage(R.string.one_photo_download_msg1)
						.setPWDialogNegativeButton(R.string.one_photo_download_no_msg1)
						.setPWDialogPositiveButton(R.string.one_photo_download_yes_msg1)
						.pwDilogShow();
			} else {
				pictureWorksDialog.setPWDialogId(DOWNLOAD_DIALOG)
						.setPWDialogMessage(R.string.one_photo_download_msg2)
						.setPWDialogNegativeButton(R.string.one_photo_download_no_msg2)
						.setPWDialogPositiveButton(R.string.one_photo_download_yes_msg2)
						.pwDilogShow();
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			returnBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void returnBack() {
		if (editMode) {
			editMode = false;
			editBarLinearLayout.setVisibility(View.GONE);
			titleTextView.setText(String.format(getString(R.string.edit_story_photo_title), photoCount));
			if (selectCount > 0) {
				for (int i = 0; i < albumArrayList.size(); i++) {
					if (albumArrayList.get(i).getIsSelected() == 1) {
						albumArrayList.get(i).setIsSelected(0);
					}
				}
				selectCount = 0;

				deleteTextView.setEnabled(false);
				downloadTextView.setEnabled(false);
				shareTextView.setEnabled(false);
				selectAllTextView.setVisibility(View.VISIBLE);
				disAllTextView.setVisibility(View.GONE);
			}
			editImageView.setVisibility(View.VISIBLE);
			if (activated == 0) {
				buyPPPRl.setVisibility(View.VISIBLE);
			}
			refreshLayout.setEnabled(true);
			pwStickySectionRecyclerView.setEditMode(editMode);
		} else {
			finish();
		}
	}

	/**
	 * 删除本地列表的数据
	 */
	private void deleteNetworkPhotos() {
		/**
		 * 1.删除数据库的操作（照片表和收藏表都要删除），同时需要判断是否输入多张PP卡
		 * 2.删除本地列表操作
		 */
		PictureAirDbManager.deletePhotosFromPhotoInfoAndFavorite(photopassPhotoslist, ppCode + ",");

		for (int i = 0; i < photopassPhotoslist.size(); i++) {
			Iterator<PhotoInfo> iterator = albumArrayList.iterator();
			while (iterator.hasNext()) {
				PhotoInfo photoInfo = iterator.next();
				if (photoInfo.getPhotoOriginalURL().equals(photopassPhotoslist.get(i).getPhotoOriginalURL())) {
					iterator.remove();
					break;
				}
			}
		}
	}

	/**
	 * 删除之后的处理
	 */
	private void dealAfterDeleted() {
		if (selectCount == 0) {
			deleteTextView.setEnabled(false);
			downloadTextView.setEnabled(false);
			shareTextView.setEnabled(false);
		}
		if (photopassPhotoslist.size() > 0) {
			if (netWorkFailed) {//网络图片删除失败
				myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
			}
		}

		if (albumArrayList.size() == 0) {//全部删除，需要显示无图页面
			editMode = false;//需要取消编辑模式
			noCountView.setVisibility(View.VISIBLE);
			noCountTextView.setText(R.string.no_photo_in_airpass);
		}

		dismissPWProgressDialog();
	}

	@Override
	public void onPWDialogButtonClicked(int which, int dialogId) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				if (dialogId == DELETE_DIALOG) {
					UmengUtil.onEvent(EditStoryAlbumActivity.this, Common.EVENT_ONCLICK_DEL_PHOTO); //统计点删除的事件。（友盟）
					showPWProgressDialog(R.string.is_loading);
					new Thread() {
						public void run() {
							photopassPhotoslist.clear();
							for (int i = 0; i < albumArrayList.size(); i++) {
								if (albumArrayList.get(i).getIsSelected() == 1) {//选中的照片
									if (albumArrayList.get(i).getIsOnLine() == 1) {//网络照片
										photopassPhotoslist.add(albumArrayList.get(i));
									} else {
									}
								}
							}

							if (photopassPhotoslist.size() > 0) {
								editStoryAlbumHandler.sendEmptyMessage(START_DELETE_NETWORK_PHOTOS);
							} else {

							}

						}
					}.start();
				} else if (dialogId == HAS_UNPAY_PHOTOS_DIALOG) {
					startDownload(true);

				} else if (dialogId == GO_DOWNLOAD_ACTIVITY_DIALOG) {
					AppManager.getInstance().killActivity(LoadManageActivity.class);
					Intent i = new Intent(EditStoryAlbumActivity.this, LoadManageActivity.class);
					startActivity(i);
					AppManager.getInstance().killActivity(MyPPActivity.class);
					finish();

				} else if (dialogId == GO_SETTING_DIALOG) {
					//去更改：跳转到设置界面。
					Intent intent = new Intent(EditStoryAlbumActivity.this, SettingActivity.class);
					startActivity(intent);

				} else if (dialogId == DOWNLOAD_DIALOG) {
					downloadPic();
				} else if (dialogId == FIRST_TEN_PHOTOS_TIP_DIALOG) {
					// 去升级：购买AirPass+页面. 由于失去了airPass详情的界面。故此处，跳转到了airPass＋的界面。
					Intent intent = new Intent();
					intent.setClass(EditStoryAlbumActivity.this, MyPPPActivity.class);
					startActivity(intent);
				}
				break;
		}
	}

	@Override
	public void loadMore() {
		if (!refreshLayout.isRefreshing() && !editMode) {//没有刷新，也没有编辑
			PictureAirLog.d("start load more---->");
			pwStickySectionRecyclerView.setIsLoadMore(true);
			pwStickySectionRecyclerView.setLoadMoreType(StickyRecycleAdapter.LOAD_MORE_LOADING);
			refreshLayout.setEnabled(false);
			getPhotosFromNetWork(API1.GET_OLD_PHOTOS, ppLoadMoreTime, ppLoadMoreIds);
		}
	}
}
