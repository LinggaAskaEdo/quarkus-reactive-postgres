package org.otis.fruit.usecase;

import java.util.UUID;

import org.otis.fruit.domain.FruitRepository;
import org.otis.shared.constant.StatusMsgEnum;
import org.otis.shared.dto.DtoPagingRequest;
import org.otis.shared.dto.DtoPagingResponse;
import org.otis.shared.util.DtoHelper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GetFruits {
	private final FruitRepository fruitRepository;

	public GetFruits(FruitRepository fruitRepository) {
		this.fruitRepository = fruitRepository;
	}

	public Uni<DtoPagingResponse> execute(DtoPagingRequest pagingRequest, UUID id) {
		return fruitRepository.getFruits(pagingRequest, id).onItem().ifNotNull()
				.transform(resp -> DtoHelper.constructPagingResponse(StatusMsgEnum.SUCCESS,
						"Data found !!!", resp.getFruits(), resp.getFruits().size(), resp.getCount()))
				.onItem().ifNull()
				.continueWith(() -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED,
						"Data not found !!!", null, 0, 0))
				.onFailure().recoverWithItem(failure -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED,
						failure.getMessage(), null, 0, 0));
	}
}
