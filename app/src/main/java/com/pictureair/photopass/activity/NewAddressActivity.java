package com.pictureair.photopass.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.SendAddress;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.CityModel;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CountyModel;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ProvinceModel;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

public class NewAddressActivity extends BaseActivity implements View.OnClickListener {
    private String AddressXML;// xml格式的中国省市区信息
    private List<ProvinceModel> provinceList; // 地址列表
    private boolean isCity = false;
    private boolean isCounty = false;
    private boolean isEnglishAdressMode = false;
    private String upName;
    private String upPhone;
    private String upProvince;
    private String upCity;
    private String upCountry;
    private String upAddress;
    private int pPosition;
    private int cPosition;
    private ImageView backIV;
    private Button provBtn,cityBtn,countryBtn;
    private EditTextWithClear nameET,phoneET,detailAddrET;
    private TextView editTv, deleteTv, addTv, titleTv;
    private SendAddress address;
    private LinearLayout provinceLL,cityLL,countryLL;
    private View provinceLine,cityLine,countryLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_address);
        initView();
        if (MyApplication.getInstance().getLanguageType().equals(Common.ENGLISH)) {
            isEnglishAdressMode = true;
            provinceLL.setVisibility(View.GONE);
            cityLL.setVisibility(View.GONE);
            countryLL.setVisibility(View.GONE);
            provinceLine.setVisibility(View.GONE);
            cityLine.setVisibility(View.GONE);
            countryLine.setVisibility(View.GONE);
        }
        initData();
        fillData();
    }
    private void fillData(){
        address=getIntent().getParcelableExtra("address");
        if(null!=address){
            nameET.setText(address.getName());
            phoneET.setText(address.getMobilePhone());
            provBtn.setText(address.getProvince());
            cityBtn.setText(address.getCity());
            countryBtn.setText(address.getCountry());
            detailAddrET.setText(address.getDetailAddress());
            isCity = true;
            isCounty = true;

            for (int i = 0; i < provinceList.size(); i++) {
                if (provinceList.get(i).getProvince().equals(address.getProvince())) {
                    pPosition = i;
                    break;
                }
            }

            for (int i = 0; i < provinceList.get(pPosition).getCity_list().size(); i++) {
                if (provinceList.get(pPosition).getCity_list().get(i).getCity().equals(address.getCity())) {
                    cPosition = i;
                    break;
                }
            }
            addTv.setVisibility(View.GONE);
            titleTv.setText(R.string.invoice_edit_address);

        } else {
            titleTv.setText(R.string.invoice_new_address);
            deleteTv.setVisibility(View.GONE);
            editTv.setVisibility(View.GONE);
            // 初始化列表下标
            pPosition = 0;
            cPosition = 0;
        }
    }
    private void initView() {
        provinceLine = findViewById(R.id.address_province_line);
        cityLine = findViewById(R.id.address_city_line);
        countryLine = findViewById(R.id.address_country_line);
        provinceLL= (LinearLayout) findViewById(R.id.address_province_ll);
        cityLL= (LinearLayout) findViewById(R.id.address_city_ll);
        countryLL= (LinearLayout) findViewById(R.id.address_country_ll);
        provBtn= (Button) findViewById(R.id.newaddress_province_btn);
        cityBtn= (Button) findViewById(R.id.newaddress_city_btn);
        countryBtn= (Button) findViewById(R.id.newaddress_country_btn);
        nameET= (EditTextWithClear) findViewById(R.id.newaddress_name_et);
        phoneET= (EditTextWithClear) findViewById(R.id.newaddress_phone_et);
        detailAddrET= (EditTextWithClear) findViewById(R.id.newaddress_detail_addr_et);
        titleTv = (TextView) findViewById(R.id.newaddress_title);
        editTv= (TextView) findViewById(R.id.newaddress_ok);
        backIV= (ImageView) findViewById(R.id.newaddress_back);
        deleteTv = (TextView) findViewById(R.id.delete_address);
        addTv = (TextView) findViewById(R.id.new_address);

        provBtn.setOnClickListener(this);
        cityBtn.setOnClickListener(this);
        countryBtn.setOnClickListener(this);
        editTv.setOnClickListener(this);
        backIV.setOnClickListener(this);
        deleteTv.setOnClickListener(this);
        addTv.setOnClickListener(this);
        setFilterListener();
    }

    public void setFilterListener(){
        nameET.addTextChangedListener(new TextWatcher() {
            int cou = 0;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cou = before + count;
                String editable = nameET.getText().toString();
                String str = AppUtil.inputTextFilter(editable); //过滤特殊字符
                if (!editable.equals(str)) {
                    nameET.setText(str);
                }
                nameET.setSelection(nameET.length());
                cou = nameET.length();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        detailAddrET.addTextChangedListener(new TextWatcher() {
            int cou = 0;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cou = before + count;
                String editable = detailAddrET.getText().toString();
                String str = AppUtil.inputTextFilter(editable); //过滤特殊字符
                if (!editable.equals(str)) {
                    detailAddrET.setText(str);
                }
                detailAddrET.setSelection(detailAddrET.length());
                cou = detailAddrET.length();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    // 加载数据
    public void initData() {
        AddressXML = getRawAddress().toString();// 获取中国省市区信息
        try {
            analysisXML(AddressXML);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取地区raw里的地址xml内容
     * */
    public StringBuffer getRawAddress() {
        InputStream in = getResources().openRawResource(R.raw.address);
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sb = new StringBuffer();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            br.close();
            isr.close();
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return sb;
    }
    /**
     * 解析省市区xml， 采用的是pull解析， 为什么选择pull解析：因为pull解析简单浅显易懂！
     * */
    public void analysisXML(String data) throws XmlPullParserException {
        try {
            ProvinceModel provinceModel = null;
            CityModel cityModel = null;
            CountyModel countyModel = null;
            List<CityModel> cityList = null;
            List<CountyModel> countyList = null;

            InputStream xmlData = new ByteArrayInputStream(
                    data.getBytes("UTF-8"));
            XmlPullParserFactory factory = null;
            factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser;
            parser = factory.newPullParser();
            parser.setInput(xmlData, "utf-8");

            String province;
            String city;
            String county;

            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                String typeName = parser.getName();

                if (type == XmlPullParser.START_TAG) {
                    if ("root".equals(typeName)) {
                        provinceList = new ArrayList<ProvinceModel>();
                    } else if ("province".equals(typeName)) {
                        province = parser.getAttributeValue(0);// 获取标签里第一个属性,例如<city
                        // name="北京市"
                        // index="1">中的name属性
                        provinceModel = new ProvinceModel();
                        provinceModel.setProvince(province);
                        cityList = new ArrayList<CityModel>();
                    } else if ("city".equals(typeName)) {
                        city = parser.getAttributeValue(0);
                        cityModel = new CityModel();
                        cityModel.setCity(city);
                        countyList = new ArrayList<CountyModel>();
                    } else if ("area".equals(typeName)) {
                        county = parser.getAttributeValue(0);
                        countyModel = new CountyModel();
                        countyModel.setCounty(county);
                    }
                } else if (type == XmlPullParser.END_TAG) {
                    if ("root".equals(typeName)) {
                    } else if ("province".equals(typeName)) {
                        provinceModel.setCity_list(cityList);
                        provinceList.add(provinceModel);
                    } else if ("city".equals(typeName)) {
                        cityModel.setCounty_list(countyList);
                        cityList.add(cityModel);
                    } else if ("area".equals(typeName)) {
                        countyList.add(countyModel);
                    }
                } else if (type == XmlPullParser.TEXT) {

                }
                type = parser.next();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.newaddress_province_btn:
                createDialog(1);
                break;
            case R.id.newaddress_city_btn:
                if (isCity) {
                    createDialog(2);
                } else {
                    new PWToast(this).setTextAndShow(R.string.invoice_tips_province, Common.TOAST_SHORT_TIME);
                }
                break;
            case R.id.newaddress_country_btn:
                if (isCounty) {
                    createDialog(3);
                } else {
                    new PWToast(this).setTextAndShow(R.string.invoice_tips_province_or_city, Common.TOAST_SHORT_TIME);
                }
                break;

            case R.id.new_address:
            case R.id.newaddress_ok:
                hideInputMethodManager(v);
                sendIntent();
                break;
            case R.id.newaddress_back:
                hideInputMethodManager(v);
                finish();
                break;

            case R.id.delete_address:
                //执行删除操作
                showPWProgressDialog();
                JSONArray ids = new JSONArray();
                ids.add(address.getAddressId());
                deleteInvoiceAddress(ids);
                break;

            default:
                break;
        }
    }

    /**
     * 删除收货地址
     * @param ids
     */
    private void deleteInvoiceAddress(JSONArray ids) {
        API2.deleteInvoiceAddress(ids)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("delete address success --> " + jsonObject.toJSONString());
                        dismissPWProgressDialog();
                        Intent intent = new Intent();
                        intent.putExtra("isDeleteAdd", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        PWToast.getInstance(NewAddressActivity.this).setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 根据调用类型显示相应的数据列表
     *
     * @param type
     *            显示类型 1.省；2.市；3.县、区
     */
    public void createDialog(final int type) {
        ListView lv = new ListView(this);
        final Dialog dialog = new Dialog(this,R.style.AddressDialog);

        if (type == 1) {
            dialog.setTitle(R.string.invoice_province);
            ProvinceAdapter pAdapter = new ProvinceAdapter(provinceList);
            lv.setAdapter(pAdapter);

        } else if (type == 2) {
            dialog.setTitle(R.string.invoice_city);
            CityAdapter cAdapter = new CityAdapter(provinceList.get(pPosition)
                    .getCity_list());
            lv.setAdapter(cAdapter);
        } else if (type == 3) {
            dialog.setTitle(R.string.invoice_country);
            CountyAdapter coAdapter = new CountyAdapter(provinceList
                    .get(pPosition).getCity_list().get(cPosition)
                    .getCounty_list());
            lv.setAdapter(coAdapter);
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (type == 1) {
                    pPosition = position;
                    provBtn.setText(provinceList.get(position)
                            .getProvince());
                    // 判断该省下是否有市级
                    if (provinceList.get(position).getCity_list().size() < 1) {
                        cityBtn.setText("");
                        countryBtn.setText("");
                        isCity = false;
                        isCounty = false;
                    } else {
                        isCity = true;
                        cityBtn.setText(provinceList.get(position)
                                .getCity_list().get(0).getCity());
                        cPosition = 0;
                        // 判断该市下是否有区级或县级
                        if (provinceList.get(position).getCity_list().get(0)
                                .getCounty_list().size() < 1) {
                            countryBtn.setText("");
                            isCounty = false;

                        } else {
                            isCounty = true;
                            countryBtn.setText(provinceList.get(position)
                                    .getCity_list().get(0).getCounty_list()
                                    .get(0).getCounty());
                        }

                    }

                } else if (type == 2) {
                    cPosition = position;
                    cityBtn.setText(provinceList.get(pPosition).getCity_list()
                            .get(position).getCity());
                    if (provinceList.get(pPosition).getCity_list()
                            .get(position).getCounty_list().size() < 1) {
                        countryBtn.setText("");
                        isCounty = false;
                    } else {
                        isCounty = true;
                        countryBtn.setText(provinceList.get(pPosition)
                                .getCity_list().get(cPosition).getCounty_list()
                                .get(0).getCounty());
                    }

                } else if (type == 3) {
                    countryBtn.setText(provinceList.get(pPosition)
                            .getCity_list().get(cPosition).getCounty_list()
                            .get(position).getCounty());

                }

                dialog.dismiss();
            }
        });

        lv.setDivider(new ColorDrawable(ContextCompat.getColor(this, R.color.pp_blue)));
        lv.setDividerHeight(1);
        dialog.setContentView(lv);
        dialog.show();
    }

    class ProvinceAdapter extends BaseAdapter {
        public List<ProvinceModel> adapter_list;

        public ProvinceAdapter(List<ProvinceModel> list) {
            adapter_list = list;
        }

        @Override
        public int getCount() {
            return adapter_list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2) {
            TextView tv = new TextView(NewAddressActivity.this);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(16);
            tv.setTextColor(getResources().getColor(R.color.pp_dark_blue));
            tv.setBackgroundResource(R.drawable.button_address_selector);
            tv.setText(adapter_list.get(position).getProvince());
            return tv;
        }

    }

    class CityAdapter extends BaseAdapter {
        public List<CityModel> adapter_list;

        public CityAdapter(List<CityModel> list) {
            adapter_list = list;
        }

        @Override
        public int getCount() {
            return adapter_list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2) {
            TextView tv = new TextView(NewAddressActivity.this);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(16);
            tv.setTextColor(getResources().getColor(R.color.pp_dark_blue));
            tv.setBackgroundResource(R.drawable.button_address_selector);
            tv.setText(adapter_list.get(position).getCity());
            return tv;
        }
    }

    class CountyAdapter extends BaseAdapter {
        public List<CountyModel> adapter_list;

        public CountyAdapter(List<CountyModel> list) {
            adapter_list = list;
        }

        @Override
        public int getCount() {
            return adapter_list.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int position, View arg1, ViewGroup arg2) {
            TextView tv = new TextView(NewAddressActivity.this);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(16);
            tv.setTextColor(getResources().getColor(R.color.pp_dark_blue));
            tv.setBackgroundResource(R.drawable.button_address_selector);
            tv.setText(adapter_list.get(position).getCounty());
            return tv;
        }
    }

    public void sendIntent(){
        if(checkData()) {
            upName = nameET.getText().toString().trim();
            upPhone = phoneET.getText().toString().trim();
            upAddress = detailAddrET.getText().toString().trim();
            if (isEnglishAdressMode) {
                upProvince = "";
                upCity = "";
                upCountry = "";
            } else {
                upProvince = provBtn.getText().toString().trim();
                upCity = cityBtn.getText().toString().trim();
                upCountry = countryBtn.getText().toString().trim();
            }

            Intent intent = new Intent();
            intent.putExtra("name", upName);
            intent.putExtra("phone", upPhone);
            intent.putExtra("province", upProvince);
            intent.putExtra("city", upCity);
            intent.putExtra("country", upCountry);
            intent.putExtra("address", upAddress);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public boolean checkData(){
        if(TextUtils.isEmpty(nameET.getText().toString())) {
            new PWToast(this).setTextAndShow(R.string.invoice_tips_input_info, Common.TOAST_SHORT_TIME);
            return false;
        }
        if(TextUtils.isEmpty(phoneET.getText().toString())) {
            new PWToast(this).setTextAndShow(R.string.invoice_tips_phone, Common.TOAST_SHORT_TIME);
            return false;
        }else{
            if(!AppUtil.checkPhoneNumber(phoneET.getText().toString())) {
                new PWToast(this).setTextAndShow(R.string.invoice_tips_phone_error, Common.TOAST_SHORT_TIME);
                return false;
            }
        }
        if(TextUtils.isEmpty(detailAddrET.getText().toString())) {
            new PWToast(this).setTextAndShow(R.string.invoice_tips_detail_address, Common.TOAST_SHORT_TIME);
            return false;
        }
        if(MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
            if (TextUtils.isEmpty(provBtn.getText().toString())) {
                new PWToast(this).setTextAndShow(R.string.invoice_tips_province, Common.TOAST_SHORT_TIME);
                return false;
            }
            if (TextUtils.isEmpty(cityBtn.getText().toString())) {
                new PWToast(this).setTextAndShow(R.string.invoice_tips_city, Common.TOAST_SHORT_TIME);
                return false;
            }
            if (TextUtils.isEmpty(countryBtn.getText().toString())) {
                new PWToast(this).setTextAndShow(R.string.invoice_tips_country, Common.TOAST_SHORT_TIME);
                return false;
            }
        }
        return true;
    }

    private void hideInputMethodManager(View v) {
		/* 隐藏软键盘 */
        InputMethodManager imm = (InputMethodManager) v.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }
}
