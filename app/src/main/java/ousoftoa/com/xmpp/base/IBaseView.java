package ousoftoa.com.xmpp.base;

/**
 * Created by 36483 on 2016/12/19.
 */

public interface IBaseView {
    /**
     * 显示提示信息
     *
     * @param msg
     */
    void showTip(String msg);

    /**
     * 成功加载结果（默认回调方法，一般在子类里自定义）
     *
     * @param object
     */
    void loadSuccess(Object object);

    /**
     * 失败加载结果（默认回调方法，一般在子类里自定义）
     *
     * @param errorMsg
     */
    void loadFailure(String errorMsg);

    /**
     * 显示加载对话框
     */
    void showLoadingDialog();

    /**
     * 关闭加载对话框
     */
    void closeLoadingDialog();

    /**
     * 加载失败提示
     */
    void onError(Throwable e);
}
