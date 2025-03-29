package com.enesuzun.javainstagramclon.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.enesuzun.javainstagramclon.R;
import com.enesuzun.javainstagramclon.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    //ActivityResultLauncher'lar: Galeri erişimi ve izin istekleri için kullanılıyor
    ActivityResultLauncher<Intent> activityResultLauncher;//ıntent üzerinden kullanacağız
    ActivityResultLauncher<String> permissionLauncher;//izin isteneceği için String
    Uri imageData;
    private ActivityUploadBinding binding;
    //Bitmap selectedBitmap;

    private FirebaseStorage firebaseStorage;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //bindingi inizalize ediyorum
        binding= ActivityUploadBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseStorage=FirebaseStorage.getInstance();
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();//Sql hizmeti
        //Storageın base referansını veriyor
        storageReference=firebaseStorage.getReference();//Zaten olusturduğumuz storagedan referans alıyoruz


        registerLauncher();

    }
    public void uploadButtonClicked(View view){
        if(imageData!=null){
            //Referance bizim storege de nereye ne kaydetmek istediğimiz sırasını tutan bir obje
            //Storage altında bir alt klasor olusturuyoruz

            //universel unique Id
            UUID uuid=UUID.randomUUID();
            String imageName="images/"+uuid+".jpg";

            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Download Url
                    StorageReference newReference=firebaseStorage.getReference(imageName);
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {//resmin indirme referasnı buradadır
                        @Override
                        public void onSuccess(Uri uri) {
                            //burada bilgileri alıyoruz
                            String downloadUrl=uri.toString();
                            String comment=binding.commentText.getText().toString();
                            FirebaseUser user=auth.getCurrentUser();
                            String email= user.getEmail();

                            //burada da post hash mapine atıyoruz
                            HashMap<String, Object> postData=new HashMap<>();
                            postData.put("useremail",email);
                            postData.put("downloadurl",downloadUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Post").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent=new Intent(UploadActivity.this,FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//her şeyi kapatır bu sayfadaki
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

    }
    public void selectImage(View view){
        //Android 33++ için READ_MEDİA_İMAGES
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            //fotoraf seçerken manifest dosyasına eklememiz şarttı
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){//izin vermediyse ne yapacağız
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    //Neden Bu izni aldığımızı göstermemiz gerekiyor
                    Snackbar.make(view ,"Permission needed galery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {//LENGTH_INDEFINITE belirsiz göster anlamında kullanıcı ne zaman tıklarsa o zaman gidecek
                        @Override
                        public void onClick(View view) {
                            //tekrar izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            }else{
                //zaten izin verilmis demektir o zaman da galerye gidilir
                Intent intentToGallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//ACTION_PICK Git ve oradan bir şey al anlamında
                activityResultLauncher.launch(intentToGallery);
            }
        }else{
            //android 33 -- -> READ_EXTERNAL_STORAGE
            //fotoraf seçerken manifest dosyasına eklememiz şarttı
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){//izin vermediyse ne yapacağız
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //Neden Bu izni aldığımızı göstermemiz gerekiyor
                    Snackbar.make(view ,"Permission needed galery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {//LENGTH_INDEFINITE belirsiz göster anlamında kullanıcı ne zaman tıklarsa o zaman gidecek
                        @Override
                        public void onClick(View view) {
                            //tekrar izin isteyeceğiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }else{
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }else{
                //zaten izin verilmis demektir o zaman da galerye gidilir
                Intent intentToGallery =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//ACTION_PICK Git ve oradan bir şey al anlamında
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    private void registerLauncher(){//Kayıt işlemi
        //ActivityResultContracts.StartActivityForResult() bir aktivite başlatacağız ama bir sonuc için başlatacağız
        //ActivityResultCallback Sonuc fonksiyonu için yazılır
        activityResultLauncher =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                //o burada sonucu temsil eder
                if(o.getResultCode()==RESULT_OK){
                    Intent intentFromResault = o.getData();//Burada veriyi alıyoruz
                    if(intentFromResault != null){//Veri bş mu değil mi kontrolu
                        imageData=intentFromResault.getData();//Uri döndürüyor Uri verinin tam lokasyonunu döndürüyor
                        //firebase bizden Uri ister
                        //kullanıcıya göstermek için bitmap kullanamız gerekiyor
                        binding.imageView.setImageURI(imageData);//bu bu uygulamada yeterlidir ama bitmap orneğine de bakalım
                    }
                }
            }
        });
        //izin alacağız 1. parametre ile 2. parametre ile ise ne yapılacağını
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if(o){
                    //izin verildi
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    //izin verilmedi
                    Toast.makeText(UploadActivity.this, "Permission needed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}






