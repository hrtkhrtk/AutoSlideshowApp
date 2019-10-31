package jp.techacademy.hirotaka.iwasaki.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    private var mHandler = Handler()

    private var cursor: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()


            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cursor!!.close()
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        //val cursor = resolver.query(
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            val imageUri = getImageUri(cursor)
            imageView.setImageURI(imageUri)



            go_button.setOnClickListener {
                if (cursor!!.moveToNext()) { // 次がある
                    val imageUri = getImageUri(cursor)
                    imageView.setImageURI(imageUri)
                } else { // 次がない
                    cursor!!.moveToFirst()
                    val imageUri = getImageUri(cursor)
                    imageView.setImageURI(imageUri)
                }
            }



            back_button.setOnClickListener {
                if (cursor!!.moveToPrevious()) { // 次がある
                    val imageUri = getImageUri(cursor)
                    imageView.setImageURI(imageUri)
                } else { // 次がない
                    cursor!!.moveToLast()
                    val imageUri = getImageUri(cursor)
                    imageView.setImageURI(imageUri)
                }
            }



            play_button.setOnClickListener {
                if (mTimer == null){
                    play_button.text = "停止"
                    go_button.setClickable(false) // 参考：https://qiita.com/loadac1978/items/f54bef2728e782e5827b
                    back_button.setClickable(false)
                    go_button.setTextColor(Color.parseColor("#BBBBBB")) // 参考：https://www.javadrive.jp/android/color/index5.html
                    back_button.setTextColor(Color.parseColor("#BBBBBB"))

                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {

                            if (cursor!!.moveToNext()) { // 次がある
                                val imageUri = getImageUri(cursor)
                                //imageView.setImageURI(imageUri)

                                mHandler.post {
                                    imageView.setImageURI(imageUri)
                                }

                            } else { // 次がない
                                cursor!!.moveToFirst()
                                val imageUri = getImageUri(cursor)
                                //imageView.setImageURI(imageUri)

                                mHandler.post {
                                    imageView.setImageURI(imageUri)
                                }

                            }
                        }
                    }, 2000, 2000) // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設定
                } else {
                    play_button.text = "再生"
                    go_button.setClickable(true) // 参考：https://qiita.com/loadac1978/items/f54bef2728e782e5827b
                    back_button.setClickable(true)
                    go_button.setTextColor(Color.parseColor("#000000")) // 参考：https://www.javadrive.jp/android/color/index5.html
                    back_button.setTextColor(Color.parseColor("#000000"))

                    mTimer!!.cancel()
                    mTimer = null
                }
            }
        }
        //cursor.close() // オープンしっぱなしだけどいいの？
    }



    private fun getImageUri(cursor: Cursor?): Uri {
        // indexからIDを取得し、そのIDから画像のURIを取得する

        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        return imageUri
    }


}
