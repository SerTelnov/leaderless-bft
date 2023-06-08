# Leaderless BFT consensus

Implementation of leaderless byzantine fault tolerance consensus algorithm  `DBFT: Efficient Leaderless Byzantine Consensus
  and its Application to Blockchains`
  
  ### Run the experiment
  
  ```
  git clone https://github.com/SerTelnov/leaderless-bft.git

  cd leaderless-bft
  ./gradlew shadowJar

  cd benchmark/
  mkdir results

  python main.py
  ```
  
  The experimental resuts:
  ```
  -----------------------------------------
 SUMMARY:
-----------------------------------------
 + CONFIG:
 Committee: 12 node(s)
 Faults: 0 node(s)
 Execution time: 57 s

 + RESULTS:
 Consensus latency: 89 ms
 Process consensus: 316
 Latency per peers:
  Peer 32ab0448-f075-45db-abfe-78901c0f72e1 mean latency 49 ms
  Peer 54edf828-78c2-469e-a9f7-f38361eaa2ee mean latency 52 ms
  Peer 0e77612c-6d9f-42b9-9215-ec28ab61ad17 mean latency 51 ms
  Peer d49f1303-ef4d-476f-9b69-045d0cb5423e mean latency 52 ms
  Peer 43112372-d726-4dd2-b55f-cad0f886fec8 mean latency 54 ms
  Peer 7fd4e38e-34d3-423f-bdf9-722bed2a4bf3 mean latency 54 ms
  Peer f77bd9d9-e3e3-4914-9cd5-180acd6ef2fe mean latency 52 ms
  Peer 2db35f7d-fd64-4d4e-9705-7be72aeeee24 mean latency 52 ms
  Peer 2909c1af-203a-4085-a1b0-5272c81554d9 mean latency 54 ms
  Peer b77dae0a-172e-4cf6-a060-3e25d77cbc95 mean latency 55 ms
  Peer c482156f-7970-4dbe-871b-c9a395f805d4 mean latency 53 ms
  Peer ff7da348-c6c0-4458-ad75-cbe4cbcf7c86 mean latency 55 ms
 Mean consensus latency for peers: 52.75 ms
-----------------------------------------
```
