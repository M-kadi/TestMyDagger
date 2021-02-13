package com.example.testmydagger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testmydagger.dataModule.MyViewModel
import com.example.testmydagger.room.UserRepository
import com.example.testmydagger.sqlite.DbHelper
import com.example.testmydagger.sqlite.User
import com.example.testmydagger.userList.UserListAdapter
import com.example.testmydagger.userList.UsersViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.input_user.view.*
import kotlinx.android.synthetic.main.list_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var car3: Car3
    @Inject
    lateinit var car2: Car2

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (application as MyApp).myComponent?.inject(this)

        Log.i("Dagger_2","${car2.maker()}")
        Log.i("Dagger_2","${car3.maker()}")

        mySharedPreferences.putData("mmm", 99)
        val value = mySharedPreferences.getData("mmm")
        Log.i("ooooooo", "" + value)

//        testViewModel.test()

//        testSqlite.test()

        testRoom.test()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    val testViewModel = TestViewModel()
    inner class TestViewModel{
        fun test(){
//        component?.inject(viewModel)
            val viewModel = ViewModelProviders.of(this@MainActivity, viewModelFactory).get(
                MyViewModel::class.java)


            lifecycle.addObserver(viewModel)
//        viewModel.showTextDataNotifier.observe(this, textDataObserver)
//        btn_fetch.setOnClickListener { viewModel.fetchValue() }
            ///
            viewModel.getCounter().observe(this@MainActivity, Observer {
                txt1122.setText("Count is "+it)
            })

            btn22.setOnClickListener {
                viewModel.addCounter()
            }
        }
    }

    @Inject
    lateinit var dbHelper: DbHelper
    val testSqlite = TestSqlite()
    inner class TestSqlite{
        // Inject Sqlite
        fun test(){
//            btnSqlite.setOnClickListener {
                val id = dbHelper.insertUser(User("mkadi"))
                val value = dbHelper.getUser(id)
                Log.i("ggggg","" + value)
                Toast.makeText(this@MainActivity, "Sqlie $value" , Toast.LENGTH_SHORT).show()
//            }
        }
    }

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var usersViewModel : UsersViewModel


    val testRoom = TestRoom()
    inner class TestRoom{
        fun test(){
//            test_show_room()
//            test_insert_room()

            testRoomUserList()
        }

        private fun testRoomUserList(){


            btnAdd.setOnClickListener {
                val btnsheet = layoutInflater.inflate(R.layout.input_user, null)
                val dialog = BottomSheetDialog(this@MainActivity)
                dialog.setContentView(btnsheet)

                fun onSave(){
                    GlobalScope.launch {
                        val user = btnsheet.editUser.text.toString()
                        when {
                            user.isBlank() ->
                                this@MainActivity.runOnUiThread(java.lang.Runnable {
                                    Toast.makeText(this@MainActivity, "Set User Name", Toast.LENGTH_SHORT)
                                        .show()
                                })
                            else -> {
                                usersViewModel.insert(user)
                                //or => //userDao.insert(com.example.dagger.room.User(user))
                            }
                        }
                    }
                }


                btnsheet.btnSave.setOnClickListener {
                    onSave()
                    btnsheet.btnSave.hideKeyboard()
                    dialog.dismiss()
                }
                dialog.show()


//            onSave()

                btnsheet.editUser.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                    val hasEnterOrGo = keyCode == KeyEvent.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_GO
                    return@OnKeyListener when (event.action == KeyEvent.ACTION_DOWN && hasEnterOrGo) {
                        true -> btnsheet.btnSave.callOnClick().let { true }
                        false -> false
                    }
                })


            }

            val adapter = UserListAdapter(this@MainActivity).apply {
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()
                        empty_view.isVisible = this@apply.itemCount == 0
                    }
                })
            }

            recyclerview.adapter = adapter
            recyclerview.layoutManager = LinearLayoutManager(this@MainActivity)

            usersViewModel.allUsers.observe(this@MainActivity) { users ->
                adapter.setWords(users)
            }
        }


        private fun test_insert_room(){
//            btn_insert_room1.setOnClickListener {
//      val wordDao = WordRoomDatabase.getDatabase(requireContext()).wordDao()
            GlobalScope.launch {
//            wordDao.run {
//
//            }
                userRepository.insert("hello10000")
                userRepository.insert("hello20000")
                userRepository.insert("hello30000")

            }
//            }
        }


        private fun test_show_room(){
            val allUsers =
                userRepository.allUsers.flowOn(Dispatchers.Main)
                    .asLiveData(context = GlobalScope.coroutineContext)

//            btn_show_room1.setOnClickListener {
            allUsers
                .observe(this@MainActivity, Observer {
                    // foo is still nullable
                    // get value of LiveData : one times : after MainActivity onCreate
                    it.forEach { Log.i("fff", "nullable " +  it.usr_name) }
                })

            allUsers
                .nonNull()
                .observe(this@MainActivity, {
                    // Now foo is non-null
                    // get value of LiveData : always
                    it.forEach { Log.i("fff", "non-null " +  it.usr_name) }

                    Log.i("fff", "non-null size " +  it.size)
                })
//            }
        }
    }

}

// room
// get value of LiveData : always
class NonNullMediatorLiveData<T> : MediatorLiveData<T>()
fun <T> LiveData<T>.nonNull(): NonNullMediatorLiveData<T> {
    val mediator: NonNullMediatorLiveData<T> = NonNullMediatorLiveData()
    mediator.addSource(this, Observer { it?.let { mediator.value = it } })
    return mediator
}
fun <T> NonNullMediatorLiveData<T>.observe(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let(observer)
    })
}

fun TextView.hideKeyboard() {
    clearFocus()
    getInputMethodManager()?.hideSoftInputFromWindow(windowToken, 0)
}

private fun TextView.getInputMethodManager() =
    ContextCompat.getSystemService(context, InputMethodManager::class.java)
