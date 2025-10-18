package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedFetcher.BreedNotFoundException {
        String url = String.format("https://dog.ceo/api/breed/%s/list", breed);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() == null) {
                throw new BreedFetcher.BreedNotFoundException(breed);
            }

            String body = response.body().string();

            // If HTTP response is not successful, consider it a failure.
            if (!response.isSuccessful()) {
                throw new BreedFetcher.BreedNotFoundException(breed);
            }

            JSONObject json = new JSONObject(body);
            String status = json.optString("status", "");

            if (!"success".equals(status)) {
                // Error responses look like:
                // {"status":"error","message":"Breed not found (main breed does not exist)","code":404}
                throw new BreedFetcher.BreedNotFoundException(breed);
            }

            // Expecting "message" to be a JSON array of sub-breeds.
            JSONArray messageArray = json.optJSONArray("message");
            if (messageArray == null) {
                // Unexpected shape: treat as no sub-breeds (or fail). We'll return empty list.
                return Collections.emptyList();
            }

            List<String> subBreeds = new ArrayList<>(messageArray.length());
            for (int i = 0; i < messageArray.length(); i++) {
                subBreeds.add(messageArray.getString(i));
            }

            return subBreeds;
        } catch (IOException e) {
            // Any I/O failure -> report as BreedNotFoundException as required.
            throw new BreedFetcher.BreedNotFoundException(breed);
        }
    }
}