package org.otis.service.impl;

import java.util.List;
import java.util.UUID;

import org.otis.constant.CommonConstant;
import org.otis.constant.StatusMsgEnum;
import org.otis.dao.FruitDao;
import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;
import org.otis.model.entity.Fruit;
import org.otis.service.FruitService;
import org.otis.util.DtoHelper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FruitServiceImpl implements FruitService {
    private final FruitDao fruitDao;

    public FruitServiceImpl(FruitDao fruitDao) {
        this.fruitDao = fruitDao;
    }

    @Override
    public Uni<DtoResponse> findAll() {
        Uni<List<Fruit>> fruits = fruitDao.findAll();

        return fruits.onItem().ifNotNull()
                .transform(resp -> DtoHelper
                        .constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data found !!!", resp))
                .onItem().ifNull()
                .continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED,
                        "Data not found !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
                        CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> findById(UUID id) {
        Uni<Fruit> fruit = fruitDao.findById(id);

        return fruit.onItem().ifNotNull()
                .transform(resp -> DtoHelper
                        .constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data found !!!", resp))
                .onItem().ifNull()
                .continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED,
                        "Data not found !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
                        CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> create(DtoRequest request) {
        Uni<UUID> response = fruitDao.create(request.getName());

        return response.onItem().ifNotNull()
                .transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS,
                        "Data created !!!", resp.toString()))
                .onItem().ifNull()
                .continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED,
                        "Data not created !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
                        CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> patch(DtoRequest request) {
        Uni<Fruit> fruit = fruitDao.patch(request);

        return fruit.onItem().ifNotNull()
                .transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS,
                        "Data updated successfully !!!", resp))
                .onItem().ifNull()
                .continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED,
                        "Data update failed !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
                        CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> deleteById(UUID id) {
        Uni<Boolean> fruit = fruitDao.deleteById(id);

        return fruit.onItem()
                .transform(deleted -> Boolean.TRUE.equals(deleted)
                        ? DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS,
                                "Data deleted successfully !!!", null)
                        : DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED,
                                "Data delete failed !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
                        CommonConstant.FAILED, failure.getMessage(), null));
    }
}
