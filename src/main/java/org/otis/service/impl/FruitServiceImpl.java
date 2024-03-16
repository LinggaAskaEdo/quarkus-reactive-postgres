package org.otis.service.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.otis.constant.CommonConstant;
import org.otis.constant.StatusMsgEnum;
import org.otis.dao.FruitDao;
import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;
import org.otis.model.entity.Fruit;
import org.otis.service.FruitService;
import org.otis.util.DtoHelper;

import java.util.List;

@ApplicationScoped
public class FruitServiceImpl implements FruitService {
    @Inject
    FruitDao fruitDao;

    @Override
    public Uni<DtoResponse> findAll() {
        Uni<List<Fruit>> fruits = fruitDao.findAll();

        return fruits
                .onItem().ifNotNull().transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data found !!!", resp))
                .onItem().ifNull().continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Data not found !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> findById(Long id) {
        Uni<Fruit> fruit = fruitDao.findById(id);

        return fruit
                .onItem().ifNotNull().transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data found !!!", resp))
                .onItem().ifNull().continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Data not found !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> create(DtoRequest request) {
        Uni<String> response = fruitDao.create(request.getName());

        return response
                .onItem().ifNotNull().transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data created !!!", resp))
                .onItem().ifNull().continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Data not created !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> patch(DtoRequest request) {
        Uni<Fruit> fruit = fruitDao.patch(request);

        return fruit
                .onItem().ifNotNull().transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data updated successfully !!!", resp))
                .onItem().ifNull().continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Data update failed !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, failure.getMessage(), null));
    }

    @Override
    public Uni<DtoResponse> deleteById(Long id) {
        Uni<Boolean> fruit = fruitDao.deleteById(id);

        return fruit
                .onItem().transform(deleted -> deleted ? DtoHelper.constructResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data deleted successfully !!!", null) :
                        DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Data delete failed !!!", null))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, failure.getMessage(), null));
    }
}
