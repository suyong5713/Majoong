import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

final markerProvider =
    StateNotifierProvider<MarkerNotifier, Set<Marker>>((ref) {
  return MarkerNotifier();
});

class MarkerNotifier extends StateNotifier<Set<Marker>> {
  MarkerNotifier() : super(Set()) {}

  addMarker(marker) {
    state.add(marker);
  }

  clearMarker() {
    state.clear();
  }
}
