package com.coheser.app.simpleclasses;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;


public class VideoThumbnailExtractor {

    public static void getThumbnailFromVideoFilePath(String videoFilePath,String position, ThumbnailListener listener) {
        new ThumbnailExtractorTask(listener).execute(videoFilePath,position);
    }

    public interface ThumbnailListener {
        void onThumbnail(Bitmap thumbnail);
    }

    private static class ThumbnailExtractorTask extends AsyncTask<String, Void, Bitmap> {
        private final ThumbnailListener listener;

        ThumbnailExtractorTask(ThumbnailListener listener) {
            this.listener = listener;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String videoFilePath = params[0];
                long timeInMicroseconds = Long.parseLong(params[1]);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoFilePath);


                Bitmap bitmap = retriever.getFrameAtTime(timeInMicroseconds, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                retriever.release();
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (listener != null) {
                listener.onThumbnail(result);
            }
        }
    }
}
