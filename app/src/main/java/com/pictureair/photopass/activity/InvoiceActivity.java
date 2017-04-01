package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.SendAddressAdapter;
import com.pictureair.photopass.entity.InvoiceInfo;
import com.pictureair.photopass.entity.SendAddress;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.NoScrollListView;
import com.pictureair.photopass.widget.PWToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class InvoiceActivity extends BaseActivity implements View.OnClickListener {
    private final static int ADD_ADDRESS=101;
    private final static int MODI_ADDRESS=102;
    //是否开发票
    private boolean checkInvoice=true;
    private RelativeLayout personRl;
    private RelativeLayout companyRl;
    private RelativeLayout nocheckRl;
    private ImageButton personIb;
    private ImageButton companyIb;
    private ImageButton noInvoice;
    private ImageView backIV;
    private EditTextWithClear editText;
    private NoScrollListView listAddress;
    private RelativeLayout sendAddressRl,newAddressRl;
    private ScrollView scrollView;
    private Button newAddressBtn;
    private Button okBtn;
    private View lineTopList;
    private List<SendAddress> listData;
    private SendAddressAdapter addressAdapter;
    private SendAddress newAddAddress;
    private ImageView arrowIv;
    private int curEditItemPosition=-1;
    //当前发票所有信息
    private InvoiceInfo invoiceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);
        initView();
        invoiceInfo = new InvoiceInfo();
        fillData();
        loadData();
    }


    private void fillData(){
        if(null != getIntent().getParcelableExtra("invoiceInfo")) {
            invoiceInfo = getIntent().getParcelableExtra("invoiceInfo");

            if (invoiceInfo.getTitle() == InvoiceInfo.PERSONAL) {
                personIb.setImageResource(R.drawable.invoice_press);
                companyIb.setImageResource(R.drawable.invoice_nor);
                if (editText.getVisibility() == View.VISIBLE)
                    editText.setVisibility(View.GONE);

            } else if (invoiceInfo.getTitle() == InvoiceInfo.COMPANY) {
                personIb.setImageResource(R.drawable.invoice_nor);
                companyIb.setImageResource(R.drawable.invoice_press);
                if (editText.getVisibility() != View.VISIBLE)
                    editText.setVisibility(View.VISIBLE);
                editText.setText(invoiceInfo.getCompanyName());
            }
            checkInvoice = invoiceInfo.isNeedInvoice();
            if (checkInvoice) {
                noInvoice.setImageResource(R.drawable.invoice_nor);
            } else {
                noInvoice.setImageResource(R.drawable.invoice_press);
            }

        }
    }

    private void initView() {
        listData = new ArrayList<>();
        scrollView= (ScrollView) findViewById(R.id.invoice_scrollview);
        personRl=(RelativeLayout) findViewById(R.id.invoice_personal_rl);
        personIb=(ImageButton) findViewById(R.id.invoice_personal_ib);
        companyRl=(RelativeLayout) findViewById(R.id.invoice_company_rl);
        companyIb=(ImageButton) findViewById(R.id.invoice_company_ib);
        nocheckRl= (RelativeLayout) findViewById(R.id.invoice_nocheck);
        noInvoice=(ImageButton) findViewById(R.id.invoice_nocheck_ib);
        editText= (EditTextWithClear) findViewById(R.id.invoice_et);
        listAddress= (NoScrollListView) findViewById(R.id.invoice_address_list);
        sendAddressRl=(RelativeLayout) findViewById(R.id.invoice_new_addr_rl);
        newAddressRl=(RelativeLayout) findViewById(R.id.invoice_new_address_rl);
        newAddressBtn= (Button) findViewById(R.id.invoice_new_address_btn);
        okBtn= (Button) findViewById(R.id.invoice_btn);
        arrowIv= (ImageView) findViewById(R.id.arrow_invoice_iv);
        backIV= (ImageView) findViewById(R.id.invoice_back);
        lineTopList=findViewById(R.id.invoice_line);

        personRl.setOnClickListener(this);
        personIb.setOnClickListener(this);
        companyRl.setOnClickListener(this);
        companyIb.setOnClickListener(this);
        nocheckRl.setOnClickListener(this);
        noInvoice.setOnClickListener(this);
        backIV.setOnClickListener(this);
        sendAddressRl.setOnClickListener(this);
        okBtn.setOnClickListener(this);

        newAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(InvoiceActivity.this,NewAddressActivity.class);
                startActivityForResult(intent,ADD_ADDRESS);
            }
        });

        setFilterListener();
        addressAdapter=new SendAddressAdapter(this,listData);
        addressAdapter.setAddressItemListener(new SendAddressAdapter.AddressItemListener() {
            @Override
            public void editItem(int position) {
                curEditItemPosition=position;
                Intent intent=new Intent(InvoiceActivity.this,NewAddressActivity.class);
                Bundle b=new Bundle();
                b.putParcelable("address", listData.get(position));
                intent.putExtras(b);
                startActivityForResult(intent,MODI_ADDRESS);
            }

            @Override
            public void clickItem(int position,SendAddress address) {
                curEditItemPosition=position;
                newAddAddress=address;
                modifyInvoiceAddress(address);
            }
        });

        listAddress.setAdapter(addressAdapter);
    }

    //加载地址
    private void loadData(){
        API2.getInvoiceAddressList()
                .map(new Func1<JSONObject, ArrayList<SendAddress>>() {

                    @Override
                    public ArrayList<SendAddress> call(JSONObject jsonObject) {
                        PictureAirLog.out("resi;t===>" + jsonObject.toJSONString());
                        return JsonUtil.getAddressList(jsonObject);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ArrayList<SendAddress>>bindToLifecycle())
                .subscribe(new RxSubscribe<ArrayList<SendAddress>>() {
                    @Override
                    public void _onNext(ArrayList<SendAddress> sendAddresses) {
                        listData.addAll(sendAddresses);
                        Collections.sort(listData);
                        addressAdapter.setCurrentIndex(0);
                        addressAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void _onError(int status) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 点击键盘之外，隐藏键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (AppUtil.isShouldHideInput(v, ev)) {
                hideInputMethodManager(v);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    private void setFilterListener(){
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideInputMethodManager(v);
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            int cou = 0;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cou = before + count;
                String editable = editText.getText().toString();
                String str = AppUtil.inputTextFilter(editable); //过滤特殊字符
                if (!editable.equals(str)) {
                    editText.setText(str);
                }
                editText.setSelection(editText.length());
                cou = editText.length();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void hideInputMethodManager(View v) {
        /* 隐藏软键盘 */
        InputMethodManager imm = (InputMethodManager) v.getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.invoice_personal_ib:
            case R.id.invoice_personal_rl:
                personIb.setImageResource(R.drawable.invoice_press);
                companyIb.setImageResource(R.drawable.invoice_nor);
                if(editText.getVisibility()==View.VISIBLE)
                    editText.setVisibility(View.GONE);

                invoiceInfo.setTitle(InvoiceInfo.PERSONAL);
                invoiceInfo.setCompanyName("");
                break;
            case R.id.invoice_company_ib:
            case R.id.invoice_company_rl:
                personIb.setImageResource(R.drawable.invoice_nor);
                companyIb.setImageResource(R.drawable.invoice_press);
                if(editText.getVisibility()!=View.VISIBLE)
                    editText.setVisibility(View.VISIBLE);

                invoiceInfo.setTitle(InvoiceInfo.COMPANY);
                break;
            case R.id.invoice_new_addr_rl:
                if(listAddress.getVisibility()==View.VISIBLE){
                    lineTopList.setVisibility(View.GONE);
                    listAddress.setVisibility(View.GONE);
                    newAddressRl.setVisibility(View.GONE);
                    arrowIv.setRotation(0);
                }else{
                    arrowIv.setRotation(90);
                    listAddress.setVisibility(View.VISIBLE);
                    lineTopList.setVisibility(View.VISIBLE);
                    newAddressRl.setVisibility(View.VISIBLE);
                    if(null!=listData && listData.size()>0)
                        addressAdapter.notifyDataSetChanged();

                }
                break;
            case R.id.invoice_nocheck_ib:
            case R.id.invoice_nocheck:
                if(checkInvoice){
                    noInvoice.setImageResource(R.drawable.invoice_press);
                }else{
                    noInvoice.setImageResource(R.drawable.invoice_nor);
                }
                checkInvoice = !checkInvoice;
                invoiceInfo.setNeedInvoice(checkInvoice);
                break;
            case R.id.invoice_back:
                finish();
                break;
            case R.id.invoice_btn:
                setInvoice();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SendAddress address = null;
        if(resultCode==RESULT_OK){
            switch (requestCode){
                case ADD_ADDRESS://新增地址
                    address=parseAddressData(data);
                    if(null!=address){
                        newAddAddress=address;
                        addInvoiceAddress(address);
                    }
                    break;
                case MODI_ADDRESS://修改地址
                    if (data.getBooleanExtra("isDeleteAdd", false)) {
                        //删除操作
                        deleteAddressItem(curEditItemPosition);

                    } else {
                        address=parseModifyAddressData(data);
                        if(null!=address){
                            newAddAddress = address;
                            modifyInvoiceAddress(address);
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 修改收货地址
     * @param modifyAddress
     */
    private void modifyInvoiceAddress(SendAddress modifyAddress) {
        API2.modifyInvoiceAddress(modifyAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("modify address success---> " + jsonObject.toJSONString());
                        updateAddressItem(newAddAddress, curEditItemPosition);
                        addressAdapter.setModifying(false);
                    }

                    @Override
                    public void _onError(int status) {//修改地址失败
                        addressAdapter.setModifying(false);
                        PWToast.getInstance(InvoiceActivity.this).setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    /**
     * 添加收货地址
     * @param address
     */
    private void addInvoiceAddress(SendAddress address) {
        API2.addInvoiceAddress(address)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<JSONObject>bindToLifecycle())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        PictureAirLog.d("addinvoice address-->" + jsonObject.toString());
                        if(jsonObject.containsKey("addressId")){
                            newAddAddress.setAddressId(jsonObject.getString("addressId"));
                        }
                        listData.add(newAddAddress);
                        addressAdapter.setCurrentIndex(listData.size()-1);
                        addressAdapter.setModifying(false);
                        addressAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void _onError(int status) {
                        PWToast.getInstance(InvoiceActivity.this).setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    //设置发票信息
    private void setInvoice(){
        if(checkData()) {
            Intent intent = new Intent();
            if (invoiceInfo.getTitle() == InvoiceInfo.COMPANY) {
                invoiceInfo.setCompanyName(editText.getText().toString());
            }
            if(null != addressAdapter && addressAdapter.getCurIndex() >= 0)
                invoiceInfo.setAddress(listData.get(addressAdapter.getCurIndex()));
            intent.putExtra("invoiceInfo", invoiceInfo);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private boolean checkData(){
        if(invoiceInfo.isNeedInvoice()) {
            if (invoiceInfo.getTitle() != InvoiceInfo.PERSONAL && invoiceInfo.getTitle() != InvoiceInfo.COMPANY) {
                new PWToast(this).setTextAndShow(R.string.invoice_tips_title, Common.TOAST_SHORT_TIME);
                return false;
            }
            if (invoiceInfo.getTitle() == InvoiceInfo.COMPANY && TextUtils.isEmpty(editText.getText().toString())) {
                PWToast.getInstance(this).setTextAndShow(R.string.invoice_input_company, Common.TOAST_SHORT_TIME);
                return false;
            }
            if (null == addressAdapter || addressAdapter.getCurIndex() < 0) {
                new PWToast(this).setTextAndShow(R.string.invoice_tips_address, Common.TOAST_SHORT_TIME);
                return false;
            }
        }
        return true;
    }

    private SendAddress parseAddressData(Intent data){
        SendAddress address=new SendAddress();
        if(null == data || TextUtils.isEmpty(data.getStringExtra("name")))
            return null;
        address.setName(data.getStringExtra("name"));
        address.setMobilePhone(data.getStringExtra("phone"));
        address.setProvince(data.getStringExtra("province"));
        address.setCity(data.getStringExtra("city"));
        address.setCountry(data.getStringExtra("country"));
        address.setDetailAddress(data.getStringExtra("address"));
        address.setSelected(true);
        return address;
    }

    private SendAddress parseModifyAddressData(Intent data){
        SendAddress address = parseAddressData(data);
        if(address == null)
            return null;
        address.setAddressId(listData.get(curEditItemPosition).getAddressId());
        address.setSelected(listData.get(curEditItemPosition).isSelected());
        address.setZip(listData.get(curEditItemPosition).getZip());
        address.setArea(listData.get(curEditItemPosition).getArea());
        address.setTelePhone(listData.get(curEditItemPosition).getTelePhone());
        address.setSelected(true);
        return address;

    }

    //更新修改后的地址
    private void updateAddressItem(SendAddress address,int position){
        SendAddress sendAddress = listData.get(position);
        sendAddress.setName(address.getName());
        sendAddress.setMobilePhone(address.getMobilePhone());
        sendAddress.setProvince(address.getProvince());
        sendAddress.setCity(address.getCity());
        sendAddress.setCountry(address.getCountry());
        sendAddress.setDetailAddress(address.getDetailAddress());
        sendAddress.setSelected(address.isSelected());
        addressAdapter.setCurrentIndex(position);
        addressAdapter.setModifying(false);
        addressAdapter.notifyDataSetChanged();
    }

    private void deleteAddressItem(int position){
        listData.remove(position);
        addressAdapter.selectDefaultIndex(position);
        addressAdapter.notifyDataSetChanged();
    }
}
