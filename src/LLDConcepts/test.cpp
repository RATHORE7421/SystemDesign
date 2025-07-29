// // - Write a function to find the length of longest continuous sequence of Integers that can be created from an unordered Array
    
// //     let arr = [2,5,4,7,9,10,13,14,15,16,18, 3,6]



// #include <bits/stdc++.h>
// using namespace std;


// int longestContinuousSequence(vector<int> &arr) {
//     int n = arr.size();
//     unordered_map<int, int>mp;

//     for(int i = 0; i<n; i++) {
//         mp[arr[i]] = i;
//     }

//     int mx_len = 0;
//     int st_idx = -1;
//     for(int i = 0; i<n; i++) {
//         int curr = arr[i];
//         while(mp.find(curr-1)!=mp.end()){
//             curr--;
//         }

//         int start = curr;
//         int curr_mx = 0;
//         for(mp.find(start)!=mp.end()) {
//             curr_mx++;
//             if(curr_mx > mx_len) {
//                 mx_len = curr_mx;
//                 st_idx = mp[start] - curr_mx;
//             }
//             start++;
//         }
//         mp.erase()
//     }

//     return mx_len;
// }

// int main() {
//     vector<int>arr = {2,5,4,7,9,10,13,14,15,16,18,3,6};
//     int ans = longestContinuousSequence(arr);
//     cout<<ans<<endl;
// }









    

