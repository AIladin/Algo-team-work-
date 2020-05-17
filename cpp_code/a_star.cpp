//
// Created by ailadin on 5/11/20.
//

#include <cstdio>
#include <cstdlib>
#include <vector>
#include <set>
#include <map>
#include <queue>

using namespace std;

typedef pair<int, int> coord;

int N;
int x[100];
int y[100];

set<coord> farmsSet;
map<int, set<int>> x_col;
map<int, set<int>> y_col;


int distance(const coord &u, const coord &v) {
    return abs(u.first - v.first) + abs(u.second - v.second);
}

void build_collisions() {
    for (int i = 0; i < N; i++) {
        x_col[x[i]].insert(y[i]);
        y_col[y[i]].insert(x[i]);
    }
}

int x_distance(const coord &a, const coord &b) {
    int dist = b.first - a.first;
    set<int> *xs = &y_col[a.second];
    if (!xs) return dist; // no collision on this line
    if (xs->empty()) return dist;
    auto col_iter = xs->begin();
    int col_dist;

    if (dist >= 0) {
        col_iter = xs->upper_bound(a.first);
        if (col_iter == xs->end()) return dist;
        else col_dist = *col_iter - a.first +  - 1;
    }
    else {
        col_iter = xs->lower_bound(a.first);
        if (col_iter == xs->end() && *xs->rbegin() < a.first) {
            col_dist = *xs->rbegin() - a.first + 1;
        } else if (col_iter != xs->end() && xs->begin() != col_iter){
            col_dist = *(--col_iter) - a.first + 1;
        } else {
            return dist;
        }
    }

    return abs(dist) < abs(col_dist) ? dist : col_dist;
}

int y_distance(const coord &a, const coord &b) {
    int dist = b.second - a.second;
    set<int> *ys = &x_col[a.first];
    if (!ys) return dist; // no collision on this line
    if (ys->empty()) return dist;
    auto col_iter = ys->begin();
    int col_dist;

    if (dist >= 0) {
        col_iter = ys->upper_bound(a.second);
        if (col_iter == ys->end()) return dist;
        else col_dist = *col_iter - a.second +  - 1;
    }
    else {
        col_iter = ys->lower_bound(a.second);
        if (col_iter == ys->end() && *ys->rbegin() < a.second) {
            col_dist = *ys->rbegin() - a.second + 1;
        } else if (col_iter != ys->end() && ys->begin() != col_iter){
            col_dist = *(--col_iter) - a.second + 1;
        } else {
            return dist;
        }
    }

    return abs(dist) < abs(col_dist) ? dist : col_dist;
}

struct Comparator {
    const coord start;
    const coord end;

    Comparator(const coord start, const coord anEnd) : start(start), end(anEnd) {}

    bool operator ()(const coord u, const coord v) const {
        return distance(end, u)*1.1 + distance(start, u) > distance(end, v)*1.1 + distance(start, v);
    }
};

int relax(const coord &from, const coord &to, map<coord, int> *relaxed) {
    priority_queue<coord, vector<coord>, Comparator> pq((Comparator(from, to)));
    map<coord, int> distances;
    set<coord> visited;
    int k = 0;
    int prev_dist = 0;

    for (const coord &farm: farmsSet) {
        if (!(farm == from || farm == to)) visited.insert(farm);
    }

    pq.push(from);
    distances.insert(make_pair(from, 0));

    while (!pq.empty()) {
        coord node = pq.top();
        pq.pop();

        if (visited.find(node) != visited.end()) continue;
        visited.insert(node);

        int dist = distances.at(node);

        if (node == to) return dist;

        if (distance(node, from) < dist && relaxed->find(node) == relaxed->end()) {

            (*relaxed)[node] = dist;
            dist = relax(from, node, relaxed);
            distances[node] = dist;
            (*relaxed)[node] = dist;

        } else if (relaxed->find(node) != relaxed->end()) {
            dist = relaxed->at(node);
            distances[node] = dist;
        }

        if (distances[node] < prev_dist) { // cutoff
            k = 0;
            prev_dist = distances[node];
        } else {
            k++;
        }
        if (k > N*N) {
            return -1;
        }

        int x_dist = x_distance(node, to);
        int y_dist = y_distance(node, to);

        coord adj;
        int tmp;
        for (int a = -1; a <= 1; a++)   // iter through adjacent nodes
            for (int b = -1; b <= 1; b++)
                if (a * b == 0 && a != b) {
                    adj = make_pair(node.first + a, node.second + b);
                    pq.push(adj);
                    if (distances.find(adj) != distances.end()) {
                        tmp = distances.at(adj);
                        if (dist + 1 < tmp) distances.insert(make_pair(adj, dist + 1));
                    } else {
                        distances.insert(make_pair(adj, dist + 1));
                    }
                }

        // add big steps
        if (x_dist != 0) {
            adj = make_pair(node.first + x_dist, node.second); // x step
            pq.push(adj);
            if (distances.find(adj) != distances.end()) {
                tmp = distances.at(adj);
                if (dist + abs(x_dist) < tmp) distances.insert(make_pair(adj, dist + abs(x_dist)));
            } else {
                distances.insert(make_pair(adj, dist + abs(x_dist)));
            }
        }

        if (y_dist != 0) {
            adj = make_pair(node.first, node.second + y_dist); // y step
            pq.push(adj);
            if (distances.find(adj) != distances.end()) {
                tmp = distances.at(adj);
                if (dist + abs(y_dist) < tmp) distances.insert(make_pair(adj, dist + abs(y_dist)));
            } else {
                distances.insert(make_pair(adj, dist + abs(y_dist)));
            }
        }
    }

    return -1;
}

int main() {
    scanf("%d", &N);
    for(int i = 0; i < N; i++) {
        scanf("%d %d", &x[i], &y[i]);
        farmsSet.insert(make_pair(x[i], y[i]));
    }
    build_collisions();
    int res = 0;
    int tmp;

    auto relaxed = map<coord, int>();

    for (int i = 1; i <= N; i++) {
        tmp = relax(make_pair(x[i-1], y[i-1]), make_pair(x[i%N], y[i%N]), &relaxed);
        if (tmp == -1) {
            printf("%d\n",-1);
            return 0;
        }
        res += tmp;
        relaxed.clear();
    }
   printf("%d\n", res);
    return 0;
}