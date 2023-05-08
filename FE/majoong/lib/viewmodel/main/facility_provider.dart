import 'dart:ui';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:majoong/common/const/key_value.dart';
import 'package:majoong/model/response/map/get_facility_response_dto.dart';
import 'package:majoong/service/remote/api/map/map_api_service.dart';
import 'package:majoong/service/remote/dio/dio_provider.dart';
import 'package:majoong/viewmodel/main/marker_provider.dart';
import 'package:majoong/viewmodel/main/review_dialog_provider.dart';

import '../../common/util/logger.dart';
import '../../model/request/map/get_facility_request_dto.dart';
import '../../model/response/base_response.dart';

// 시설물 조회 api RequestDto
final centerPositionProvider = StateProvider<GetFacilityRequestDto>((ref) {
  return GetFacilityRequestDto(centerLng: 0, centerLat: 0, radius: 0);
});

final cameraMovedProvider = StateProvider<bool>((ref) {
  return false;
});

final facilityProvider =
    StateNotifierProvider<FacilityNotifier, BaseResponseState>((ref) {
  final mapService = ref.watch(mapApiServiceProvider);
  final markerInfo = ref.watch(markerProvider.notifier);
  final chipInfo = ref.watch(chipProvider.notifier);
  final reviewDialogInfo = ref.watch(reviewDialogProvider.notifier);
  final centerPositionInfo = ref.watch(centerPositionProvider.notifier);
  final dio = ref.watch(dioProvider);
  final facilityNotifier = FacilityNotifier(
    dio,
    service: mapService,
    markerNotifier: markerInfo,
    chipNotifier: chipInfo,
    centerPositionNotifier: centerPositionInfo,
    reviewDialogNotifier: reviewDialogInfo,
  );
  return facilityNotifier;
});

class FacilityNotifier extends StateNotifier<BaseResponseState> {
  final MapApiService service;
  final MarkerNotifier markerNotifier;
  final ChipNotifier chipNotifier;
  final StateNotifier centerPositionNotifier;
  final ReviewDialogNotifier reviewDialogNotifier;
  final Dio dio;

  FacilityNotifier(this.dio,
      {required this.service,
      required this.markerNotifier,
      required this.chipNotifier,
      required this.centerPositionNotifier,
      required this.reviewDialogNotifier})
      : super(BaseResponseLoading()) {}

  getFacility() async {
    state = BaseResponseLoading();
    final request = centerPositionNotifier.state;
    final BaseResponse<GetFacilityResponseDto> response =
        await service.getFacility(request);
    if (response.status == 200) {
      state = response;
      final cctvList = response.data?.cctv ?? [];
      final policeList = response.data?.police ?? [];
      final lampList = response.data?.lamp ?? [];
      final cctvIcon = await BitmapDescriptor.fromAssetImage(
          ImageConfiguration(), 'res/cctv.png');

      for (var cctv in cctvList) {
        markerNotifier.addCctvMarker(Marker(
          markerId: MarkerId(cctv.cctvId.toString()),
          position: LatLng(cctv.lat, cctv.lng),
          icon: cctvIcon,
          infoWindow: InfoWindow(title: cctv.address),
        ));
      }

      final policeIcon = await BitmapDescriptor.fromAssetImage(
          ImageConfiguration(), 'res/police.png');
      for (var police in policeList) {
        markerNotifier.addPoliceMarker(Marker(
          markerId: MarkerId(police.policeId.toString()),
          position: LatLng(police.lat, police.lng),
          icon: policeIcon,
          infoWindow: InfoWindow(title: police.address),
        ));
      }

      final lampIcon = await BitmapDescriptor.fromAssetImage(
          ImageConfiguration(), 'res/lamp.png');
      for (var lamp in lampList) {
        markerNotifier.addLampMarker(Marker(
          markerId: MarkerId(lamp.lampId.toString()),
          position: LatLng(lamp.lat, lamp.lng),
          icon: lampIcon,
          infoWindow: InfoWindow(title: lamp.address),
        ));
      }
      markerNotifier.renderMarker();
    }
  }

  postReview() async {
    final request = reviewDialogNotifier.state;
    logger.d(request.reviewImage.toString());

    final formData = FormData.fromMap({
      'lng': request.lng.toString(),
      'lat': request.lat.toString(),
      'content': request.content,
      'score': request.score.toString(),
      'isBright': request.isBright.toString(),
      'isCrowded': request.isCrowded.toString(),
      'address': request.address,
      'reviewImage': request.reviewImage != null
          ? await MultipartFile.fromFile('request.reviewImage!.path',
              filename: null)
          : null,
    });

    dio.options.headers.addAll({AUTHORIZATION: AUTH});
    final response =
        await dio.post('https://majoong4u.com/api/map/review', data: formData);
    logger.d(response);
  }

  Future<BitmapDescriptor> getCustomMarkerIcon() async {
    final Size imageSize = Size(30, 30);
    final PictureRecorder pictureRecorder = PictureRecorder();
    final Canvas canvas = Canvas(pictureRecorder);

    final Paint paint = Paint()
      ..color = Colors.yellow.withOpacity(0.2)
      ..style = PaintingStyle.fill;

    canvas.drawCircle(
      Offset(imageSize.width / 2, imageSize.height / 2),
      imageSize.width / 2,
      paint,
    );

    final img = await pictureRecorder.endRecording().toImage(
          imageSize.width.toInt(),
          imageSize.height.toInt(),
        );
    final data = await img.toByteData(format: ImageByteFormat.png);
    final bytes = data?.buffer.asUint8List();

    return BitmapDescriptor.fromBytes(bytes!);
  }
}
