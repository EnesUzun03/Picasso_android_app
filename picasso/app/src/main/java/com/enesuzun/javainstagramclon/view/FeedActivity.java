package com.enesuzun.javainstagramclon.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.enesuzun.javainstagramclon.R;
import com.enesuzun.javainstagramclon.adapter.PostAdapter;
import com.enesuzun.javainstagramclon.databinding.ActivityFeedBinding;
import com.enesuzun.javainstagramclon.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    //recycler view görmek için binding yapıyoruz

    private ActivityFeedBinding binding;

    PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding=ActivityFeedBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        postArrayList=new ArrayList<>();
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        getData();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postArrayList);
        binding.recyclerView.setAdapter(postAdapter);
    }


    private void getData(){
        firebaseFirestore.collection("Post").orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    //Bir olay gerçekleşirse
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(FeedActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                        if (value != null) {
                            postArrayList.clear();
                            for (DocumentSnapshot snapshot : value.getDocuments()) {
                                Map<String, Object> data = snapshot.getData();

                                String userEmail = (String) data.get("useremail");
                                String comment = (String) data.get("comment");
                                String downloadUrl = (String) data.get("downloadurl");
                                ArrayList<String> likedBy = (ArrayList<String>) data.get("likedBy");
                                ArrayList<String> savedBy = (ArrayList<String>) data.get("savedBy");
                                
                                Post post = new Post(userEmail, comment, downloadUrl);
                                post.setId(snapshot.getId());
                                
                                // Beğeni bilgilerini yükle
                                if (likedBy != null) {
                                    for (String email : likedBy) {
                                        post.addLike(email);
                                    }
                                }
                                
                                // Kaydetme bilgilerini yükle
                                if (savedBy != null) {
                                    for (String email : savedBy) {
                                        post.addSave(email);
                                    }
                                }
                                
                                postArrayList.add(post);
                            }
                            postAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
    //Menuyu feedActivitye bağlıyoruz
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=getMenuInflater();//xml ile bağlama metotu
        menuInflater.inflate(R.menu.option_menu,menu);//menu onCreateOptionsMenu Fonksiyonunun parametresi
        return super.onCreateOptionsMenu(menu);
    }
    //menuden sçince ne olacağını yazıyoruz
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_post){
            Intent intentToUpload=new Intent(FeedActivity.this,UploadActivity.class);
            startActivity(intentToUpload);
        } else if (item.getItemId()==R.id.profile) {
            Intent intentToProfile = new Intent(FeedActivity.this, ProfileActivity.class);
            startActivity(intentToProfile);
        } else if (item.getItemId()==R.id.signout) {
            auth.signOut();
            Intent intentToMain = new Intent(FeedActivity.this,MainActivity.class);
            startActivity(intentToMain);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}









