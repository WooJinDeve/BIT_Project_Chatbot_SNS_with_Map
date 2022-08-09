//포스팅 전체 출력
package com.test.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.test.chatapp.activities.ProfileActivity;
import com.test.chatapp.activities.ReceivePostActivity;
import com.test.chatapp.databinding.ItemContainerFeedBinding;
import com.test.chatapp.databinding.ItemContainerFeedVideoBinding;
import com.test.chatapp.databinding.ItemContainerMyfeedBinding;
import com.test.chatapp.databinding.ItemContainerMyfeedVideoBinding;
import com.test.chatapp.models.Post;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Post> posts;
    private final String posterId;
    private FirebaseFirestore database;
    public static final int VIEW_TYPE_SENT_POST = 1;
    public static final int VIEW_TYPE_RECEIVED_POST = 2;
    public static final int VIEW_TYPE_SENT_POST_VIDEO = 3;
    public static final int VIEW_TYPE_RECEIVED_POST_VIDEO = 4;

    public PostAdapter(List<Post> posts, String posterId) {
        this.posts = posts;
        this.posterId = posterId;
    }

    //onCreate
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT_POST) {
            return new PostAdapter.SentPostViewHolder(
                    ItemContainerMyfeedBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_RECEIVED_POST) {
            return new PostAdapter.ReceivedPostViewHolder(
                    ItemContainerFeedBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_SENT_POST_VIDEO) {
            return new PostAdapter.SentPostVideoViewHolder(
                    ItemContainerMyfeedVideoBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new PostAdapter.ReceivedPostVideoViewHolder(
                    ItemContainerFeedVideoBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    //받은 정보 출력
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT_POST) {
            ((PostAdapter.SentPostViewHolder) holder).setData(posts.get(position));

        } else if (getItemViewType(position) == VIEW_TYPE_SENT_POST_VIDEO) {
            ((PostAdapter.SentPostVideoViewHolder) holder).setData(posts.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_POST_VIDEO) {
            ((PostAdapter.ReceivedPostVideoViewHolder) holder).setData(posts.get(position));
        } else {
            ((PostAdapter.ReceivedPostViewHolder) holder).setData(posts.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    //타입설정
    @Override
    public int getItemViewType(int position) {
        if (posts.get(position).userid.equals(posterId)) {
            if (posts.get(position).postImage.contains("postvideo"))
                return VIEW_TYPE_SENT_POST_VIDEO;
            return VIEW_TYPE_SENT_POST;
        } else {
            if (posts.get(position).postImage.contains("postvideo"))
                return VIEW_TYPE_RECEIVED_POST_VIDEO;
            return VIEW_TYPE_RECEIVED_POST;
        }
    }

    //받은 정보(내포스팅) 표현
    static class SentPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerMyfeedBinding binding;

        SentPostViewHolder(ItemContainerMyfeedBinding itemContainerMyfeedBinding) {
            super(itemContainerMyfeedBinding.getRoot());
            binding = itemContainerMyfeedBinding;
        }

        void setData(Post posts) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            PreferenceManager preferenceManager = new PreferenceManager(binding.getRoot().getContext());
            firebaseImageLoader(binding.postImageMessage.getContext(), posts.postImage, binding.postImageMessage);
            binding.postImageProfile.setImageBitmap(StringtoBitmap(posts.image));
            binding.postTextName.setText(posts.name);
            try {
                binding.postTextLocation.setText(posts.address.getAddressLine(0));
            } catch (Exception e) {
                binding.postTextLocation.setText("-");
            }
            PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);


            ArrayList<String> hashTagArray = new ArrayList<>();

            String[] tempLists;
            tempLists = posts.hashTags.split("#");

            for (String tempList : tempLists
            ) {
                if (tempList.isEmpty()) {
                } else
                    hashTagArray.add(tempList);
            }

            HashTagAdapter hashTagAdapter = new HashTagAdapter(hashTagArray);
            binding.hashTagRecyclerView.setAdapter(hashTagAdapter);


            binding.userMessageView.setAdapter(postUserMessageAdapter);
            binding.postTextMessage.setText(posts.postcontents);
            binding.postLikeNum.setText("좋아요 " + posts.postLike.size() + "개");
            binding.postUserName.setText(posts.name);
            binding.placeName.setText(posts.placeName);

            binding.postDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection("post").document(posts.postId).delete();
                    Intent intent;
                    if (binding.getRoot().getContext().toString().contains("ProfileActivity")) {
                        intent = new Intent(binding.getRoot().getContext(), ProfileActivity.class);
                    } else {
                        intent = new Intent(binding.getRoot().getContext(), ReceivePostActivity.class);
                    }
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
            binding.postInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.postDelete.setVisibility(View.VISIBLE);
                }
            });
            binding.postInfoMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.userMessageLayout.setVisibility(View.VISIBLE);
                    binding.postTextDateTime.setText(posts.dateTime);
                    binding.postTextDateTime.setVisibility(View.VISIBLE);
                    binding.postInfoMessage.setVisibility(View.GONE);
                    binding.hashTagRecyclerView.setVisibility(View.VISIBLE);
                    binding.placeNameLayout.setVisibility(View.VISIBLE);
                }
            });
            binding.userMessageSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.userMessage.getText().toString().isEmpty())
                        return;

                    database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            posts.userMessage = (List<String>) documentSnapshot.getData().get("message");

                            posts.userMessage.add(preferenceManager.getString(Constants.KEY_NAME) + "#" + preferenceManager.getString(Constants.KEY_IMAGE) + "#" +
                                    binding.userMessage.getText() + "#" + getReadableDateTime(new Date()));
                            binding.userMessage.setText("");

                            database.collection(Constants.KEY_COLLECTION_POST)
                                    .document(posts.postId).update("message", posts.userMessage)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.userMessageView.setAdapter(null);
                                            PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);
                                            binding.userMessageView.setAdapter(postUserMessageAdapter);
                                        }
                                    });
                        }
                    });
                }
            });

            binding.postLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            List<String> postLike = (List<String>) task.getResult().get("postLike");
                            postLike.add(preferenceManager.getString(Constants.KEY_NAME));

                            database.collection("post").document(posts.postId).update("postLike", postLike)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.postLikeNum.setText("좋아요 " + postLike.size() + "개");
                                        }
                                    });
                        }
                    });
                }
            });
        }
    }


    //받은 정보(내비디오포스팅) 표현
    static class SentPostVideoViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerMyfeedVideoBinding binding;

        SentPostVideoViewHolder(ItemContainerMyfeedVideoBinding itemContainerMyfeedVideoBinding) {
            super(itemContainerMyfeedVideoBinding.getRoot());
            binding = itemContainerMyfeedVideoBinding;
        }

        void setData(Post posts) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            PreferenceManager preferenceManager = new PreferenceManager(binding.getRoot().getContext());
            firebaseVideoLoader(binding.postVideo.getContext(), posts.postImage, binding.postVideo);
            binding.postImageProfile.setImageBitmap(StringtoBitmap(posts.image));
            binding.postTextName.setText(posts.name);
            try {
                binding.postTextLocation.setText(posts.address.getAddressLine(0));
            } catch (Exception e) {
                binding.postTextLocation.setText("-");
            }
            PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);


            ArrayList<String> hashTagArray = new ArrayList<>();

            String[] tempLists;
            tempLists = posts.hashTags.split("#");

            for (String tempList : tempLists
            ) {
                if (tempList.isEmpty()) {
                } else
                    hashTagArray.add(tempList);
            }

            HashTagAdapter hashTagAdapter = new HashTagAdapter(hashTagArray);
            binding.hashTagRecyclerView.setAdapter(hashTagAdapter);


            binding.userMessageView.setAdapter(postUserMessageAdapter);
            binding.postTextMessage.setText(posts.postcontents);
            binding.postLikeNum.setText("좋아요 " + posts.postLike.size() + "개");
            binding.postUserName.setText(posts.name);
            binding.placeName.setText(posts.placeName);

            binding.postDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection("post").document(posts.postId).delete();
                    Intent intent;
                    if (binding.getRoot().getContext().toString().contains("ProfileActivity")) {
                        intent = new Intent(binding.getRoot().getContext(), ProfileActivity.class);
                    } else {
                        intent = new Intent(binding.getRoot().getContext(), ReceivePostActivity.class);
                    }
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
            binding.postInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.postDelete.setVisibility(View.VISIBLE);
                }
            });
            binding.postInfoMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.userMessageLayout.setVisibility(View.VISIBLE);
                    binding.postTextDateTime.setText(posts.dateTime);
                    binding.postTextDateTime.setVisibility(View.VISIBLE);
                    binding.postInfoMessage.setVisibility(View.GONE);
                    binding.hashTagRecyclerView.setVisibility(View.VISIBLE);
                    binding.placeNameLayout.setVisibility(View.VISIBLE);
                }
            });
            binding.userMessageSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.userMessage.getText().toString().isEmpty())
                        return;

                    database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            posts.userMessage = (List<String>) documentSnapshot.getData().get("message");

                            posts.userMessage.add(preferenceManager.getString(Constants.KEY_NAME) + "#" + preferenceManager.getString(Constants.KEY_IMAGE) + "#" +
                                    binding.userMessage.getText() + "#" + getReadableDateTime(new Date()));
                            binding.userMessage.setText("");

                            database.collection(Constants.KEY_COLLECTION_POST)
                                    .document(posts.postId).update("message", posts.userMessage)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.userMessageView.setAdapter(null);
                                            PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);
                                            binding.userMessageView.setAdapter(postUserMessageAdapter);
                                        }
                                    });
                        }
                    });
                }
            });

            binding.postLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            List<String> postLike = (List<String>) task.getResult().get("postLike");
                            postLike.add(preferenceManager.getString(Constants.KEY_NAME));

                            database.collection("post").document(posts.postId).update("postLike", postLike)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.postLikeNum.setText("좋아요 " + postLike.size() + "개");
                                        }
                                    });
                        }
                    });
                }
            });
        }
    }

    //받은 정보(친구포스팅) 표현
    static class ReceivedPostViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerFeedBinding binding;

        ReceivedPostViewHolder(ItemContainerFeedBinding itemContainerFeedBinding) {
            super(itemContainerFeedBinding.getRoot());
            binding = itemContainerFeedBinding;
        }

        void setData(Post posts) {
            try {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                PreferenceManager preferenceManager = new PreferenceManager(binding.getRoot().getContext());
                firebaseImageLoader(binding.postImageMessage.getContext(), posts.postImage, binding.postImageMessage);
                binding.postImageProfile.setImageBitmap(StringtoBitmap(posts.image));
                binding.postTextName.setText(posts.name);
                binding.postTextLocation.setText(posts.address.getAddressLine(0));
                PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);
                binding.userMessageView.setAdapter(postUserMessageAdapter);
                binding.postLikeNum.setText("좋아요 " + posts.postLike.size() + "개");
                binding.postUserName.setText(posts.name);
                binding.postTextMessage.setText(posts.postcontents);
                binding.placeName.setText(posts.placeName);


                binding.postInfoMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binding.userMessageLayout.setVisibility(View.VISIBLE);
                        binding.postTextDateTime.setText(posts.dateTime);
                        binding.postTextDateTime.setVisibility(View.VISIBLE);
                        binding.postInfoMessage.setVisibility(View.GONE);
                        binding.hashTagRecyclerView.setVisibility(View.VISIBLE);
                        binding.placeNameLayout.setVisibility(View.VISIBLE);
                    }
                });
                binding.userMessageSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (binding.userMessage.getText().toString().isEmpty())
                            return;
                        database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                posts.userMessage = (List<String>) documentSnapshot.getData().get("message");

                                posts.userMessage.add(preferenceManager.getString(Constants.KEY_NAME) + "#" + preferenceManager.getString(Constants.KEY_IMAGE) + "#" +
                                        binding.userMessage.getText() + "#" + getReadableDateTime(new Date()));
                                binding.userMessage.setText("");

                                database.collection(Constants.KEY_COLLECTION_POST)
                                        .document(posts.postId).update("message", posts.userMessage)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                binding.userMessageView.setAdapter(null);
                                                PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);
                                                binding.userMessageView.setAdapter(postUserMessageAdapter);
                                            }
                                        });
                            }
                        });
                    }
                });

                binding.postLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                List<String> postLike = (List<String>) task.getResult().get("postLike");
                                postLike.add(preferenceManager.getString(Constants.KEY_NAME));

                                database.collection("post").document(posts.postId).update("postLike", postLike)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                binding.postLikeNum.setText("좋아요 " + postLike.size() + "개");
                                            }
                                        });
                            }
                        });
                    }
                });
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    //받은 정보(내비디오포스팅) 표현
    static class ReceivedPostVideoViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerFeedVideoBinding binding;

        ReceivedPostVideoViewHolder(ItemContainerFeedVideoBinding itemContainerFeedVideoBinding) {
            super(itemContainerFeedVideoBinding.getRoot());
            binding = itemContainerFeedVideoBinding;
        }

        void setData(Post posts) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            PreferenceManager preferenceManager = new PreferenceManager(binding.getRoot().getContext());
            firebaseVideoLoader(binding.postVideo.getContext(), posts.postImage, binding.postVideo);
            binding.postImageProfile.setImageBitmap(StringtoBitmap(posts.image));
            binding.postTextName.setText(posts.name);
            try {
                binding.postTextLocation.setText(posts.address.getAddressLine(0));
            } catch (Exception e) {
                binding.postTextLocation.setText("-");
            }
            PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);


            ArrayList<String> hashTagArray = new ArrayList<>();

            String[] tempLists;
            tempLists = posts.hashTags.split("#");

            for (String tempList : tempLists
            ) {
                if (tempList.isEmpty()) {
                } else
                    hashTagArray.add(tempList);
            }

            HashTagAdapter hashTagAdapter = new HashTagAdapter(hashTagArray);
            binding.hashTagRecyclerView.setAdapter(hashTagAdapter);


            binding.userMessageView.setAdapter(postUserMessageAdapter);
            binding.postTextMessage.setText(posts.postcontents);
            binding.postLikeNum.setText("좋아요 " + posts.postLike.size() + "개");
            binding.postUserName.setText(posts.name);
            binding.placeName.setText(posts.placeName);
            binding.postInfoMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.userMessageLayout.setVisibility(View.VISIBLE);
                    binding.postTextDateTime.setText(posts.dateTime);
                    binding.postTextDateTime.setVisibility(View.VISIBLE);
                    binding.postInfoMessage.setVisibility(View.GONE);
                    binding.hashTagRecyclerView.setVisibility(View.VISIBLE);
                    binding.placeNameLayout.setVisibility(View.VISIBLE);
                }
            });
            binding.userMessageSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.userMessage.getText().toString().isEmpty())
                        return;

                    database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            posts.userMessage = (List<String>) documentSnapshot.getData().get("message");

                            posts.userMessage.add(preferenceManager.getString(Constants.KEY_NAME) + "#" + preferenceManager.getString(Constants.KEY_IMAGE) + "#" +
                                    binding.userMessage.getText() + "#" + getReadableDateTime(new Date()));
                            binding.userMessage.setText("");

                            database.collection(Constants.KEY_COLLECTION_POST)
                                    .document(posts.postId).update("message", posts.userMessage)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.userMessageView.setAdapter(null);
                                            PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(posts.userMessage);
                                            binding.userMessageView.setAdapter(postUserMessageAdapter);
                                        }
                                    });
                        }
                    });
                }
            });

            binding.postLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.collection("post").document(posts.postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            List<String> postLike = (List<String>) task.getResult().get("postLike");
                            postLike.add(preferenceManager.getString(Constants.KEY_NAME));

                            database.collection("post").document(posts.postId).update("postLike", postLike)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.postLikeNum.setText("좋아요 " + postLike.size() + "개");
                                        }
                                    });
                        }
                    });
                }
            });
        }
    }

    //파이어베이스 스토리지에서 이미지 원본 가져오기 + 표현
    private static void firebaseImageLoader(Context context, String Path, ImageView image) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child(Path);
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Glide.with(context).load(uri).into(image);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }


    //파이어베이스 스토리지에서 동영상 원본 가져오기 + 표현
    private static void firebaseVideoLoader(Context context, String Path, PlayerView pv) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child(Path);
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
                    //플레이어뷰에게 플레이어 설정
                    pv.setPlayer(player);
                    //비디오데이터 소스를 관리하는 DataSource 객체를 만들어주는 팩토리 객체 생성
                    DataSource.Factory factory = new DefaultDataSourceFactory(context, "Ex89VideoAndExoPlayer");
                    //비디오데이터를 Uri로 부터 추출해서 DataSource객체 (CD or LP판 같은 ) 생성
                    ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
                    //만들어진 비디오데이터 소스객체인 mediaSource를
                    //플레이어 객체에게 전당하여 준비하도록!![ 로딩하도록 !!]
                    player.prepare(mediaSource);
                    //로딩이 완료되어 준비가 되었을 때
                    //자동 실행되도록..

                } catch (Exception e) {

                }

            }
        });
    }

    //날짜 형식 변환
    private static String getReadableDateTime(Date date) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(date);
    }

    //스트링을 이미지로 변환
    private static Bitmap StringtoBitmap(String image) {
        byte[] bytes = Base64.getDecoder().decode(image);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return bitmap;
    }

}