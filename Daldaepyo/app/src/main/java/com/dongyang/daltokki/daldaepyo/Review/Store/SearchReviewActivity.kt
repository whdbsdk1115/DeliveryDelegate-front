package com.dongyang.daltokki.daldaepyo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dongyang.daltokki.daldaepyo.Review.Store.SearchReviewAdapter
import com.dongyang.daltokki.daldaepyo.retofit.BoardItem
import com.dongyang.daltokki.daldaepyo.retrofit.SearchResponseDto
import com.dongyang.daltokki.daldaepyo.retrofit.UserAPI
import com.naver.maps.geometry.Tm128
import kotlinx.android.synthetic.main.activity_search_board.*
import kotlinx.android.synthetic.main.item_search_review.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 리스트뷰: https://philosopher-chan.tistory.com/1009

class SearchReviewActivity :AppCompatActivity() {

    val api = UserAPI.create()

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)
        setContentView(R.layout.activity_search_board)

        btn_searchBoard1.setOnClickListener {

            val storePref = getSharedPreferences("store", 0) // 음식점 정보
            val pref = getSharedPreferences("pref", 0)
            val tok = pref.getString("token", "").toString()
            var name = edt_search_board.text.toString()

            api.getSearch(tok, name).enqueue(object: Callback<SearchResponseDto> {
                override fun onResponse(call: Call<SearchResponseDto>,
                                        response: Response<SearchResponseDto>) {

                    val result_size = response.body()?.result?.size
                    val code = response.code()

                    if(code == 500) {
                        Toast.makeText(this@SearchReviewActivity, "검색에 실패했습니다. 관리자에게 문의해 주세요.", Toast.LENGTH_SHORT).show()
                    }

                    val list = mutableListOf<BoardItem>()

                    for(i in 0 until result_size!!) {

                        val before_title = response?.body()?.result?.get(i)?.title.toString()
                        val title = isPalindrome(before_title)
                        list.add(BoardItem(title));

                    }
                    // 리스트뷰에 넣어주기(보여주기)
                    val adapter = SearchReviewAdapter(this, list)
                    store_list_view.adapter = adapter

                    store_list_view.onItemClickListener = object: OnItemClickListener {

                        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                            val before_title = response?.body()?.result?.get(position)?.title.toString()
                            val title = isPalindrome(before_title)
                            val address = response?.body()?.result?.get(position)?.roadAddress.toString()
                            val xmap = response?.body()?.result?.get(position)?.mapx?.toDouble()!!
                            val ymap = response?.body()?.result?.get(position)?.mapy?.toDouble()!!

                            val tm128 = Tm128(xmap, ymap) // 좌표는 Double 형태로 넣어주어야 함
                            val latLng = tm128.toLatLng() // LatLng{latitude = , lngitude= } 형태

                            val lat = latLng.latitude.toString() // 위도만 빼주기
                            val lng = latLng.longitude.toString() // 경도만 빼주기

                            var edit = storePref.edit() // 수정모드
                            edit.apply()
                            edit.putString("title", title)
                            edit.putString("address", address)
                            edit.putString("lat", lat)
                            edit.putString("lng", lng)
                            edit.commit()

                            Log.d("title@@", "" + title)
                            Log.d("title@@", "" + address)
                            Log.d("title@@", "" + lat)
                            Log.d("title@@", "" + lng)

                            var intent = Intent(this@SearchReviewActivity, StoreDetailActivity::class.java)
                            startActivity(intent)
                            finish()

                        }

                    }
                }

                override fun onFailure(call: Call<SearchResponseDto>, t: Throwable) {
                    Log.e("검색", "${t.localizedMessage}")
                }
            })

        }
    }

    fun isPalindrome(s: String) : String {
        val regex = Regex("[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]")
        val regex2 = Regex("b")
        val result1 = regex.replace(s, "")
        val result = regex2.replace(result1, "")
        return result
    }

}
